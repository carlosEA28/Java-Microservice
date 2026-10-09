package com.example.carlos.order_service.service;

import com.example.carlos.order_service.client.InventoryServiceClient;
import com.example.carlos.order_service.entity.Order;
import com.example.carlos.order_service.event.BookingEvent;
import com.example.carlos.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private InventoryServiceClient inventoryServiceClient;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        inventoryServiceClient = mock(InventoryServiceClient.class);
        orderService = new OrderService(orderRepository, inventoryServiceClient);
    }

    @Test
    void savesBookingBeforeUpdatingInventory() {
        OrderRepository repository = mock(OrderRepository.class);
        InventoryServiceClient inventoryClient = mock(InventoryServiceClient.class);
        OrderService service = new OrderService(repository, inventoryClient);
        BookingEvent event = new BookingEvent(10L, 20L, 3L, new BigDecimal("75.00"));

        service.orderEvent(event);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        InOrder calls = inOrder(repository, inventoryClient);
        calls.verify(repository).saveAndFlush(orderCaptor.capture());
        calls.verify(inventoryClient).updateInventory(20L, 3L);

        Order saved = orderCaptor.getValue();
        assertEquals(10L, saved.getCustomerId());
        assertEquals(20L, saved.getEventId());
        assertEquals(3L, saved.getTicketCount());
        assertEquals(new BigDecimal("75.00"), saved.getTotalPrice());
    }

    @Test
    void orderEvent_createsOrderWithCorrectFields() {
        // Given
        BookingEvent event = new BookingEvent(42L, 100L, 5L, new BigDecimal("250.00"));

        // When
        orderService.orderEvent(event);

        // Then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(42L, savedOrder.getCustomerId());
        assertEquals(100L, savedOrder.getEventId());
        assertEquals(5L, savedOrder.getTicketCount());
        assertEquals(new BigDecimal("250.00"), savedOrder.getTotalPrice());
        // placedAt is set by @CreationTimestamp on persist, which doesn't happen with mock repository
    }

    @Test
    void orderEvent_callsInventoryServiceClientWithCorrectParameters() {
        // Given
        BookingEvent event = new BookingEvent(1L, 99L, 2L, new BigDecimal("100.00"));

        // When
        orderService.orderEvent(event);

        // Then
        verify(inventoryServiceClient).updateInventory(99L, 2L);
    }

    @Test
    void orderEvent_savesOrderBeforeCallingInventoryClient() {
        // Given
        BookingEvent event = new BookingEvent(7L, 88L, 1L, new BigDecimal("50.00"));
        InOrder inOrder = inOrder(orderRepository, inventoryServiceClient);

        // When
        orderService.orderEvent(event);

        // Then
        inOrder.verify(orderRepository).saveAndFlush(any(Order.class));
        inOrder.verify(inventoryServiceClient).updateInventory(88L, 1L);
    }

    @Test
    void orderEvent_handlesZeroTicketCount() {
        // Given
        BookingEvent event = new BookingEvent(5L, 55L, 0L, BigDecimal.ZERO);

        // When
        orderService.orderEvent(event);

        // Then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(5L, savedOrder.getCustomerId());
        assertEquals(55L, savedOrder.getEventId());
        assertEquals(0L, savedOrder.getTicketCount());
        assertEquals(BigDecimal.ZERO, savedOrder.getTotalPrice());

        verify(inventoryServiceClient).updateInventory(55L, 0L);
    }

    @Test
    void orderEvent_handlesLargeValues() {
        // Given
        Long customerId = 999999999L;
        Long eventId = 888888888L;
        Long ticketCount = 10000L;
        BigDecimal totalPrice = new BigDecimal("999999999.99");
        BookingEvent event = new BookingEvent(customerId, eventId, ticketCount, totalPrice);

        // When
        orderService.orderEvent(event);

        // Then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(customerId, savedOrder.getCustomerId());
        assertEquals(eventId, savedOrder.getEventId());
        assertEquals(ticketCount, savedOrder.getTicketCount());
        assertEquals(totalPrice, savedOrder.getTotalPrice());

        verify(inventoryServiceClient).updateInventory(eventId, ticketCount);
    }
}