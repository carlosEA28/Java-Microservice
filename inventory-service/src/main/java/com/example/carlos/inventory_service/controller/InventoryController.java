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

    @GetMapping("/iventory/events")
    public @ResponseBody List<EventInventoryResponse> inventoryGetAllEvents () {
        return inventoryService.getAllEvents();
    }

    @GetMapping("/iventory/venues/{venueId}")
    public @ResponseBody VenueInventoryResponse inventoryGetAllEventsByVenueId (@PathVariable Long venueId) {
        return inventoryService.getVenueEvents(venueId);
    }
}
