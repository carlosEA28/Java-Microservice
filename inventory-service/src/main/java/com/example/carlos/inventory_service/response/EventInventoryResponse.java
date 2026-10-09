package com.example.carlos.inventory_service.response;

import com.example.carlos.inventory_service.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventInventoryResponse {

    private String event;
    private Long capacity;
    private Venue venue;
}
