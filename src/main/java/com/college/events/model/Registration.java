package com.college.events.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A registration doubles as the participation record (attended flag + timestamp). */
@Entity
@Table(name = "registrations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "username"}))
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    private boolean attended;
    private LocalDateTime attendedAt;
    private LocalDateTime registeredAt = LocalDateTime.now();

    public Registration() {}

    public Registration(Event event, String username) {
        this.event = event;
        this.username = username;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public LocalDateTime getAttendedAt() { return attendedAt; }
    public void setAttendedAt(LocalDateTime attendedAt) { this.attendedAt = attendedAt; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
