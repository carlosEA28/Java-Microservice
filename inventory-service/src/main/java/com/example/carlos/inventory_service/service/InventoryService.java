package com.example.carlos.inventory_service.service;

import com.example.carlos.inventory_service.entity.Event;
import com.example.carlos.inventory_service.entity.Venue;
import com.example.carlos.inventory_service.repository.EventRepository;
import com.example.carlos.inventory_service.repository.VenueRepository;
import com.example.carlos.inventory_service.response.EventInventoryResponse;
import com.example.carlos.inventory_service.response.VenueInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public InventoryService(EventRepository eventRepository, VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventInventoryResponse> getAllEvents(){
        final List<Event> events = eventRepository.findAll();

        return events.stream().map(event -> EventInventoryResponse.builder()
                    .event(event.getName())
                    .capacity(event.getLeftCapacity()   )
                    .venue((event.getVenue() != null) ? event.getVenue() : null)
                    .build()).collect(Collectors.toList());
    }

    public VenueInventoryResponse getVenueEvents(final Long venueId){
        final Venue venue = venueRepository.findById(venueId).orElse(null);

       return VenueInventoryResponse.builder().
               VenueId(venue.getId())
               .venueName(venue.getName())
               .TotalCapacity(venue.getTotalCapacity())
               .build();


    }
}