package com.example.carlos.inventory_service.service;

import com.example.carlos.inventory_service.entity.Event;
import com.example.carlos.inventory_service.entity.Venue;
import com.example.carlos.inventory_service.repository.EventRepository;
import com.example.carlos.inventory_service.repository.VenueRepository;
import com.example.carlos.inventory_service.response.EventInventoryResponse;
import com.example.carlos.inventory_service.response.VenueInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                    .eventId(event.getId())
                    .event(event.getName())
                    .capacity(event.getLeftCapacity())
                    .venue(event.getVenue())
                    .ticketPrice(event.getTicketPrice())
                    .build()).collect(Collectors.toList());
    }

    public EventInventoryResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return EventInventoryResponse.builder()
                .eventId(event.getId())
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .build();
    }

    @Transactional
    public void decreaseCapacity(Long eventId, Long ticketCount) {
        if (ticketCount == null || ticketCount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket count must be positive");
        }
        if (eventRepository.decreaseCapacity(eventId, ticketCount) == 0) {
            if (!eventRepository.existsById(eventId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough inventory");
        }
    }

    public VenueInventoryResponse getVenueEvents(final Long venueId){
        final Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

       return VenueInventoryResponse.builder().
               venueId(venue.getId())
               .venueName(venue.getName())
               .totalCapacity(venue.getTotalCapacity())
               .build();


    }
}
