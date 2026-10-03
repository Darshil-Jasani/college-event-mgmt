package com.college.events.service;

import com.college.events.exception.BadRequestException;
import com.college.events.exception.NotFoundException;
import com.college.events.model.Event;
import com.college.events.model.Registration;
import com.college.events.model.RegistrationStatus;
import com.college.events.repository.RegistrationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final RegistrationRepository registrations;
    private final EventService eventService;

    public RegistrationService(RegistrationRepository registrations, EventService eventService) {
        this.registrations = registrations;
        this.eventService = eventService;
    }

    public Registration register(Long eventId, String username) {
        Event event = eventService.get(eventId);
        Registration existing = registrations.findByEventIdAndUsername(eventId, username).orElse(null);
        if (existing != null && existing.getStatus() == RegistrationStatus.REGISTERED) {
            throw new BadRequestException("Already registered for this event");
        }
        long taken = registrations.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
        if (taken >= event.getCapacity()) {
            throw new BadRequestException("Event is full");
        }
        Registration reg = existing != null ? existing : new Registration(event, username);
        reg.setStatus(RegistrationStatus.REGISTERED);
        reg.setRegisteredAt(LocalDateTime.now());
        return registrations.save(reg);
    }

    public Registration cancel(Long eventId, String username) {
        Registration reg = registrations.findByEventIdAndUsername(eventId, username)
                .orElseThrow(() -> new NotFoundException("No registration found"));
        reg.setStatus(RegistrationStatus.CANCELLED);
        return registrations.save(reg);
    }

    public List<Registration> forEvent(Long eventId) {
        eventService.get(eventId);
        return registrations.findByEventId(eventId);
    }

    public List<Registration> forUser(String username) {
        return registrations.findByUsername(username);
    }

    public Registration markAttendance(Long eventId, String username, boolean attended) {
        Registration reg = registrations.findByEventIdAndUsername(eventId, username)
                .orElseThrow(() -> new NotFoundException("No registration found"));
        if (reg.getStatus() != RegistrationStatus.REGISTERED) {
            throw new BadRequestException("Registration is cancelled");
        }
        reg.setAttended(attended);
        reg.setAttendedAt(attended ? LocalDateTime.now() : null);
        return registrations.save(reg);
    }
}
