package com.college.events.controller;

import com.college.events.model.Registration;
import com.college.events.service.RegistrationService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) { this.service = service; }

    @PostMapping("/events/{eventId}/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Registration register(@PathVariable Long eventId, Principal principal) {
        return service.register(eventId, principal.getName());
    }

    @DeleteMapping("/events/{eventId}/register")
    public Registration cancel(@PathVariable Long eventId, Principal principal) {
        return service.cancel(eventId, principal.getName());
    }

    @GetMapping("/events/{eventId}/registrations")
    public List<Registration> forEvent(@PathVariable Long eventId) {
        return service.forEvent(eventId);
    }

    @PostMapping("/events/{eventId}/attendance/{username}")
    public Registration attendance(@PathVariable Long eventId, @PathVariable String username,
                                   @RequestParam(defaultValue = "true") boolean attended) {
        return service.markAttendance(eventId, username, attended);
    }

    @GetMapping("/me/registrations")
    public List<Registration> mine(Principal principal) {
        return service.forUser(principal.getName());
    }
}
