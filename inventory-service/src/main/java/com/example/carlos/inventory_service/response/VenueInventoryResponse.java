package com.example.carlos.inventory_service.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueInventoryResponse {
    private Long VenueId;
    private String venueName;
    private Long TotalCapacity;
}
