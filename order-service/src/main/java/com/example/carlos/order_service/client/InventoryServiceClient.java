package com.example.carlos.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class InventoryServiceClient {

    private final RestClient restClient;

    public InventoryServiceClient(@Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(inventoryServiceUrl).build();
    }

    public void updateInventory(Long eventId, Long ticketCount) {
        restClient.put()
                .uri("/event/{eventId}/capacity/{ticketCount}", eventId, ticketCount)
                .retrieve()
                .toBodilessEntity();
    }
}
