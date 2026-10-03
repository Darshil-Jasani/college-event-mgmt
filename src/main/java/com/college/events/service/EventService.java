package com.college.events.service;

import com.college.events.exception.NotFoundException;
import com.college.events.model.Event;
import com.college.events.repository.EventRepository;
import com.college.events.repository.RegistrationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository events;
    private final RegistrationRepository registrations;

    public EventService(EventRepository events, RegistrationRepository registrations) {
        this.events = events;
        this.registrations = registrations;
    }

    public List<Event> findAll() { return events.findAll(); }

    public Event get(Long id) {
        return events.findById(id).orElseThrow(() -> new NotFoundException("Event " + id + " not found"));
    }

    public Event create(Event event, String username) {
        event.setId(null);
        event.setCreatedBy(username);
        return events.save(event);
    }

    public Event update(Long id, Event input) {
        Event existing = get(id);
        existing.setTitle(input.getTitle());
        existing.setDescription(input.getDescription());
        existing.setVenue(input.getVenue());
        existing.setEventDate(input.getEventDate());
        existing.setCapacity(input.getCapacity());
        return events.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Event existing = get(id);
        registrations.deleteByEventId(id);
        events.delete(existing);
    }
}
