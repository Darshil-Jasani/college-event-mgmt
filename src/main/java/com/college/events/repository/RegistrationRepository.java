package com.college.events.repository;

import com.college.events.model.Registration;
import com.college.events.model.RegistrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByUsername(String username);
    Optional<Registration> findByEventIdAndUsername(Long eventId, String username);
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
    void deleteByEventId(Long eventId);
}
