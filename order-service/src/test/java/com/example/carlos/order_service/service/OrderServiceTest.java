package com.example.carlos.order_service.service;

import com.example.carlos.order_service.client.InventoryServiceClient;
import com.example.carlos.order_service.entity.Order;
import com.example.carlos.order_service.event.BookingEvent;
import com.example.carlos.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class OrderServiceTest {

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
}
