package com.utms.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findByEmailAndStatus(String email, String status);

    Optional<PasswordResetRequest> findByResetToken(String resetToken);

    long countByEmailAndCreatedAtAfter(String email, Instant createdAt);
}
