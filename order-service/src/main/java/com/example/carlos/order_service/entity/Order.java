package com.example.carlos.order_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "quantity", nullable = false)
    private Long ticketCount;

    @CreationTimestamp
    @Column(name = "placed_at", updatable = false, nullable = false)
    private LocalDateTime placedAt;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    protected Order() {
    }

    public Order(Long customerId, Long eventId, Long ticketCount, BigDecimal totalPrice) {
        this.customerId = customerId;
        this.eventId = eventId;
        this.ticketCount = ticketCount;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public Long getTicketCount() { return ticketCount; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public Long getCustomerId() { return customerId; }
    public Long getEventId() { return eventId; }
}
