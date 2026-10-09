package com.example.carlos.inventory_service.controller;

import com.example.carlos.inventory_service.response.EventInventoryResponse;
import com.example.carlos.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
}
