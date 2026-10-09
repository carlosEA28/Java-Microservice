package com.example.carlos.order_service.service;

import com.example.carlos.order_service.client.InventoryServiceClient;
import com.example.carlos.order_service.entity.Order;
import com.example.carlos.order_service.event.BookingEvent;
import com.example.carlos.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderService(OrderRepository orderRepository, InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received order event: {}", bookingEvent);
        Order order = new Order(bookingEvent.userId(), bookingEvent.eventId(),
                bookingEvent.ticketCount(), bookingEvent.totalPrice());
        orderRepository.saveAndFlush(order);
        inventoryServiceClient.updateInventory(order.getEventId(), order.getTicketCount());
        log.info("Inventory updated for event: {}, less tickets: {}", order.getEventId(), order.getTicketCount());
    }
}
