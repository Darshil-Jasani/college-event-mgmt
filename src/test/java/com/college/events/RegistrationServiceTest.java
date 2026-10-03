package com.college.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.college.events.exception.BadRequestException;
import com.college.events.model.Event;
import com.college.events.model.Registration;
import com.college.events.model.RegistrationStatus;
import com.college.events.repository.RegistrationRepository;
import com.college.events.service.EventService;
import com.college.events.service.RegistrationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationServiceTest {

    private RegistrationRepository repo;
    private EventService eventService;
    private RegistrationService service;
    private Event event;

    @BeforeEach
    void setUp() {
        repo = mock(RegistrationRepository.class);
        eventService = mock(EventService.class);
        service = new RegistrationService(repo, eventService);
        event = new Event("Hackathon", "24h", "Lab 1", LocalDateTime.now().plusDays(3), 2);
        when(eventService.get(1L)).thenReturn(event);
        when(repo.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registersWhenSeatsAvailable() {
        when(repo.findByEventIdAndUsername(1L, "student")).thenReturn(Optional.empty());
        when(repo.countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED)).thenReturn(0L);

        Registration r = service.register(1L, "student");

        assertEquals(RegistrationStatus.REGISTERED, r.getStatus());
        assertEquals("student", r.getUsername());
    }

    @Test
    void rejectsWhenEventFull() {
        when(repo.findByEventIdAndUsername(1L, "student")).thenReturn(Optional.empty());
        when(repo.countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED)).thenReturn(2L);

        assertThrows(BadRequestException.class, () -> service.register(1L, "student"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        when(repo.findByEventIdAndUsername(1L, "student"))
                .thenReturn(Optional.of(new Registration(event, "student")));

        assertThrows(BadRequestException.class, () -> service.register(1L, "student"));
    }

    @Test
    void marksAttendance() {
        Registration reg = new Registration(event, "student");
        when(repo.findByEventIdAndUsername(1L, "student")).thenReturn(Optional.of(reg));

        Registration r = service.markAttendance(1L, "student", true);

        assertTrue(r.isAttended());
        assertNotNull(r.getAttendedAt());
    }

    @Test
    void cannotMarkAttendanceForCancelledRegistration() {
        Registration reg = new Registration(event, "student");
        reg.setStatus(RegistrationStatus.CANCELLED);
        when(repo.findByEventIdAndUsername(1L, "student")).thenReturn(Optional.of(reg));

        assertThrows(BadRequestException.class, () -> service.markAttendance(1L, "student", true));
    }
}
