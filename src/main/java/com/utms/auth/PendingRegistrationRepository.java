package com.utms.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByVerificationSessionId(String verificationSessionId);
    List<PendingRegistration> findByEmailAndStatus(String email, String status);
}
