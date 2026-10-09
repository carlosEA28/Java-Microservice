package com.example.carlos.inventory_service.service;

import com.example.carlos.inventory_service.entity.Event;
import com.example.carlos.inventory_service.entity.Venue;
import com.example.carlos.inventory_service.repository.EventRepository;
import com.example.carlos.inventory_service.repository.VenueRepository;
import com.example.carlos.inventory_service.response.EventInventoryResponse;
import com.example.carlos.inventory_service.response.VenueInventoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private EventRepository eventRepository;
    private VenueRepository venueRepository;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        venueRepository = mock(VenueRepository.class);
        inventoryService = new InventoryService(eventRepository, venueRepository);
    }

    @Test
    void getAllEvents_returnsListOfEventInventoryResponses() {
        // Given
        Venue venue = new Venue(1L, "Arena", "123 Main St", 5000L);
        Event event1 = new Event(10L, "Concert", "123 Main St", 1000L, 800L,
                new BigDecimal("50.00"), venue);
        Event event2 = new Event(20L, "Theater", "123 Main St", 500L, 300L,
                new BigDecimal("75.00"), venue);

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

        // When
        List<EventInventoryResponse> result = inventoryService.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        EventInventoryResponse response1 = result.get(0);
        assertEquals(10L, response1.getEventId());
        assertEquals("Concert", response1.getEvent());
        assertEquals(800L, response1.getCapacity());
        assertEquals(venue, response1.getVenue());
        assertEquals(new BigDecimal("50.00"), response1.getTicketPrice());

        EventInventoryResponse response2 = result.get(1);
        assertEquals(20L, response2.getEventId());
        assertEquals("Theater", response2.getEvent());
        assertEquals(300L, response2.getCapacity());
        assertEquals(new BigDecimal("75.00"), response2.getTicketPrice());

        verify(eventRepository).findAll();
    }

    @Test
    void getAllEvents_whenNoEvents_returnsEmptyList() {
        // Given
        when(eventRepository.findAll()).thenReturn(List.of());

        // When
        List<EventInventoryResponse> result = inventoryService.getAllEvents();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository).findAll();
    }

    @Test
    void getEvent_whenEventExists_returnsEventInventoryResponse() {
        // Given
        Long eventId = 10L;
        Venue venue = new Venue(1L, "Stadium", "456 Oak Ave", 10000L);
        Event event = new Event(eventId, "Football Match", "456 Oak Ave", 5000L, 4500L,
                new BigDecimal("100.00"), venue);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When
        EventInventoryResponse result = inventoryService.getEvent(eventId);

        // Then
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals("Football Match", result.getEvent());
        assertEquals(4500L, result.getCapacity());
        assertEquals(venue, result.getVenue());
        assertEquals(new BigDecimal("100.00"), result.getTicketPrice());

        verify(eventRepository).findById(eventId);
    }

    @Test
    void getEvent_whenEventNotFound_throwsNotFoundException() {
        // Given
        Long eventId = 999L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.getEvent(eventId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Event not found", exception.getReason());

        verify(eventRepository).findById(eventId);
    }

    @Test
    void decreaseCapacity_whenValidRequest_updatesCapacity() {
        // Given
        Long eventId = 10L;
        Long ticketCount = 5L;

        when(eventRepository.decreaseCapacity(eventId, ticketCount)).thenReturn(1);

        // When
        inventoryService.decreaseCapacity(eventId, ticketCount);

        // Then
        verify(eventRepository).decreaseCapacity(eventId, ticketCount);
        verify(eventRepository, never()).existsById(anyLong());
    }

    @Test
    void decreaseCapacity_whenNullTicketCount_throwsBadRequestException() {
        // Given
        Long eventId = 10L;

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.decreaseCapacity(eventId, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Ticket count must be positive", exception.getReason());

        verify(eventRepository, never()).decreaseCapacity(anyLong(), anyLong());
    }

    @Test
    void decreaseCapacity_whenZeroTicketCount_throwsBadRequestException() {
        // Given
        Long eventId = 10L;

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.decreaseCapacity(eventId, 0L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Ticket count must be positive", exception.getReason());

        verify(eventRepository, never()).decreaseCapacity(anyLong(), anyLong());
    }

    @Test
    void decreaseCapacity_whenNegativeTicketCount_throwsBadRequestException() {
        // Given
        Long eventId = 10L;

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.decreaseCapacity(eventId, -1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Ticket count must be positive", exception.getReason());

        verify(eventRepository, never()).decreaseCapacity(anyLong(), anyLong());
    }

    @Test
    void decreaseCapacity_whenNotEnoughCapacity_throwsConflictException() {
        // Given
        Long eventId = 10L;
        Long ticketCount = 5L;

        when(eventRepository.decreaseCapacity(eventId, ticketCount)).thenReturn(0);
        when(eventRepository.existsById(eventId)).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.decreaseCapacity(eventId, ticketCount));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Not enough inventory", exception.getReason());

        verify(eventRepository).decreaseCapacity(eventId, ticketCount);
        verify(eventRepository).existsById(eventId);
    }

    @Test
    void decreaseCapacity_whenEventNotFound_throwsNotFoundException() {
        // Given
        Long eventId = 999L;
        Long ticketCount = 5L;

        when(eventRepository.decreaseCapacity(eventId, ticketCount)).thenReturn(0);
        when(eventRepository.existsById(eventId)).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.decreaseCapacity(eventId, ticketCount));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Event not found", exception.getReason());

        verify(eventRepository).decreaseCapacity(eventId, ticketCount);
        verify(eventRepository).existsById(eventId);
    }

    @Test
    void getVenueEvents_whenVenueExists_returnsVenueInventoryResponse() {
        // Given
        Long venueId = 1L;
        Venue venue = new Venue(venueId, "Convention Center", "789 Elm St", 2000L);

        when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));

        // When
        VenueInventoryResponse result = inventoryService.getVenueEvents(venueId);

        // Then
        assertNotNull(result);
        assertEquals(venueId, result.getVenueId());
        assertEquals("Convention Center", result.getVenueName());
        assertEquals(2000L, result.getTotalCapacity());

        verify(venueRepository).findById(venueId);
    }

    @Test
    void getVenueEvents_whenVenueNotFound_throwsNotFoundException() {
        // Given
        Long venueId = 999L;
        when(venueRepository.findById(venueId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryService.getVenueEvents(venueId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Venue not found", exception.getReason());

        verify(venueRepository).findById(venueId);
    }
}