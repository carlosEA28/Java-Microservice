package com.example.carlos.inventory_service.controller;

import com.example.carlos.inventory_service.response.EventInventoryResponse;
import com.example.carlos.inventory_service.response.VenueInventoryResponse;
import com.example.carlos.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/inventory/events")
    public @ResponseBody List<EventInventoryResponse> inventoryGetAllEvents () {
        return inventoryService.getAllEvents();
    }

    @GetMapping("/inventory/venue/{venueId}")
    public @ResponseBody VenueInventoryResponse inventoryGetAllEventsByVenueId (@PathVariable Long venueId) {
        return inventoryService.getVenueEvents(venueId);
    }

    @GetMapping("/inventory/event/{eventId}")
    public EventInventoryResponse getEvent(@PathVariable Long eventId) {
        return inventoryService.getEvent(eventId);
    }

    @PutMapping("/inventory/event/{eventId}/capacity/{ticketCount}")
    public void decreaseCapacity(@PathVariable Long eventId, @PathVariable Long ticketCount) {
        inventoryService.decreaseCapacity(eventId, ticketCount);
    }
}
