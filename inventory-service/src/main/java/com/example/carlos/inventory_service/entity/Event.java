package com.example.carlos.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event")
public class Event {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "total_capacity")
    private Long totalCapacity;

    @Column(name = "left_capacity")
    private Long leftCapacity;

    @Column(name = "ticket_price")
    private BigDecimal ticketPrice;

    @ManyToOne
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    private Venue venue;
}
