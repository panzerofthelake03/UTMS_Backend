package com.utms.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /** UC 1.7 SR-3: rate limiting — count tickets created within the window for a student */
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.student.id = :studentId AND t.createdAt > :since")
    long countRecentByStudentId(@Param("studentId") Long studentId, @Param("since") Instant since);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
