package com.example.carlos.order_service.event;

import java.math.BigDecimal;

public record BookingEvent(Long userId, Long eventId, Long ticketCount, BigDecimal totalPrice) {
}
