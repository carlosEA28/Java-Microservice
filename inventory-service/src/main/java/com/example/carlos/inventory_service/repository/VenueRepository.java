package com.example.carlos.inventory_service.repository;

import com.example.carlos.inventory_service.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
