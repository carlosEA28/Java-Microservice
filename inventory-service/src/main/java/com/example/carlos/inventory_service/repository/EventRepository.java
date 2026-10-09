package com.example.carlos.inventory_service.repository;

import com.example.carlos.inventory_service.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Modifying
    @Query("update Event e set e.leftCapacity = e.leftCapacity - :count where e.id = :id and e.leftCapacity >= :count")
    int decreaseCapacity(@Param("id") Long id, @Param("count") Long count);
}
