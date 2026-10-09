package com.example.carlos.booking_service.service;

import com.example.carlos.booking_service.client.InventoryServiceClient;
import com.example.carlos.booking_service.entity.Customer;
import com.example.carlos.booking_service.event.BookingEvent;
import com.example.carlos.booking_service.repository.CustomerRepository;
import com.example.carlos.booking_service.request.BookingRequest;
import com.example.carlos.booking_service.response.BookingResponse;
import com.example.carlos.booking_service.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;


    public BookingService(final CustomerRepository customerRepository,
                          final InventoryServiceClient inventoryServiceClient,
                          final KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public BookingResponse createBooking(final BookingRequest request) {
        if (request.getUserId() == null || request.getEventId() == null
                || request.getTicketCount() == null || request.getTicketCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User, event and a positive ticket count are required");
        }
        // check if user exists
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        // check if there is enough inventory
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        log.info("Inventory Response: {}", inventoryResponse);
        if (inventoryResponse.getCapacity() < request.getTicketCount()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough inventory");
        }
        // create booking event (for response)
        final BookingEvent bookingEvent = createBookingEvent(request, customer, inventoryResponse);
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Booking created: {}", bookingEvent);
        return BookingResponse.builder()
                .userId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }

    private BookingEvent createBookingEvent(final BookingRequest request,
                                            final Customer customer,
                                            final InventoryResponse inventoryResponse) {
        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketCount(request.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketCount())))
                .build();
    }
}
