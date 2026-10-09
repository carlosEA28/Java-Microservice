package com.example.carlos.booking_service.service;

import com.example.carlos.booking_service.client.InventoryServiceClient;
import com.example.carlos.booking_service.entity.Customer;
import com.example.carlos.booking_service.event.BookingEvent;
import com.example.carlos.booking_service.repository.CustomerRepository;
import com.example.carlos.booking_service.request.BookingRequest;
import com.example.carlos.booking_service.response.BookingResponse;
import com.example.carlos.booking_service.response.InventoryResponse;
import com.example.carlos.booking_service.response.VenueResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private CustomerRepository customerRepository;
    private InventoryServiceClient inventoryServiceClient;
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        inventoryServiceClient = mock(InventoryServiceClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        bookingService = new BookingService(customerRepository, inventoryServiceClient, kafkaTemplate);
    }

    @Test
    void createBooking_whenValidRequest_returnsBookingResponse() {
        // Given
        Long userId = 1L;
        Long eventId = 10L;
        Long ticketCount = 2L;
        BigDecimal ticketPrice = new BigDecimal("50.00");
        BigDecimal expectedTotalPrice = ticketPrice.multiply(BigDecimal.valueOf(ticketCount));

        Customer customer = Customer.builder().id(userId).name("John Doe").email("john@example.com").build();
        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .ticketCount(ticketCount)
                .build();

        VenueResponse venue = VenueResponse.builder().id(1L).name("Arena").build();
        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .eventId(eventId)
                .event("Concert")
                .capacity(100L)
                .venue(venue)
                .ticketPrice(ticketPrice)
                .build();

        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));
        when(inventoryServiceClient.getInventory(eventId)).thenReturn(inventoryResponse);
        when(kafkaTemplate.send(anyString(), any(BookingEvent.class))).thenReturn(null);

        // When
        BookingResponse response = bookingService.createBooking(request);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(eventId, response.getEventId());
        assertEquals(ticketCount, response.getTicketCount());
        assertEquals(expectedTotalPrice, response.getTotalPrice());

        verify(customerRepository).findById(userId);
        verify(inventoryServiceClient).getInventory(eventId);
        verify(kafkaTemplate).send(eq("booking"), any(BookingEvent.class));
    }

    @Test
    void createBooking_whenUserNotFound_throwsNotFoundException() {
        // Given
        Long userId = 999L;
        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(10L)
                .ticketCount(2L)
                .build();

        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());

        verify(customerRepository).findById(userId);
        verify(inventoryServiceClient, never()).getInventory(anyLong());
        verify(kafkaTemplate, never()).send(anyString(), any(BookingEvent.class));
    }

    @Test
    void createBooking_whenNullUserId_throwsBadRequestException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .userId(null)
                .eventId(10L)
                .ticketCount(2L)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User, event and a positive ticket count are required", exception.getReason());

        verify(customerRepository, never()).findById(anyLong());
        verify(inventoryServiceClient, never()).getInventory(anyLong());
        verify(kafkaTemplate, never()).send(anyString(), any(BookingEvent.class));
    }

    @Test
    void createBooking_whenZeroTicketCount_throwsBadRequestException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .userId(1L)
                .eventId(10L)
                .ticketCount(0L)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User, event and a positive ticket count are required", exception.getReason());
    }

    @Test
    void createBooking_whenNegativeTicketCount_throwsBadRequestException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .userId(1L)
                .eventId(10L)
                .ticketCount(-1L)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User, event and a positive ticket count are required", exception.getReason());
    }

    @Test
    void createBooking_whenNotEnoughInventory_throwsConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 10L;
        Long ticketCount = 150L;

        Customer customer = Customer.builder().id(userId).name("John Doe").email("john@example.com").build();
        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .ticketCount(ticketCount)
                .build();

        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .eventId(eventId)
                .event("Concert")
                .capacity(100L)
                .ticketPrice(new BigDecimal("50.00"))
                .build();

        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));
        when(inventoryServiceClient.getInventory(eventId)).thenReturn(inventoryResponse);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Not enough inventory", exception.getReason());

        verify(customerRepository).findById(userId);
        verify(inventoryServiceClient).getInventory(eventId);
        verify(kafkaTemplate, never()).send(anyString(), any(BookingEvent.class));
    }

    @Test
    void createBooking_whenNullEventId_throwsBadRequestException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .userId(1L)
                .eventId(null)
                .ticketCount(2L)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User, event and a positive ticket count are required", exception.getReason());
    }

    @Test
    void createBooking_sendsCorrectBookingEventToKafka() {
        // Given
        Long userId = 1L;
        Long eventId = 10L;
        Long ticketCount = 3L;
        BigDecimal ticketPrice = new BigDecimal("25.00");
        BigDecimal expectedTotalPrice = ticketPrice.multiply(BigDecimal.valueOf(ticketCount));

        Customer customer = Customer.builder().id(userId).name("Jane Doe").email("jane@example.com").build();
        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .ticketCount(ticketCount)
                .build();

        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .eventId(eventId)
                .event("Theater Play")
                .capacity(200L)
                .ticketPrice(ticketPrice)
                .build();

        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));
        when(inventoryServiceClient.getInventory(eventId)).thenReturn(inventoryResponse);
        when(kafkaTemplate.send(anyString(), any(BookingEvent.class))).thenReturn(null);

        ArgumentCaptor<BookingEvent> eventCaptor = ArgumentCaptor.forClass(BookingEvent.class);

        // When
        bookingService.createBooking(request);

        // Then
        verify(kafkaTemplate).send(eq("booking"), eventCaptor.capture());
        BookingEvent sentEvent = eventCaptor.getValue();

        assertEquals(userId, sentEvent.getUserId());
        assertEquals(eventId, sentEvent.getEventId());
        assertEquals(ticketCount, sentEvent.getTicketCount());
        assertEquals(expectedTotalPrice, sentEvent.getTotalPrice());
    }
}