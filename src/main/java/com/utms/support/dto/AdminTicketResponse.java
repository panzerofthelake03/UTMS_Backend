package com.utms.support.dto;

import java.time.Instant;

public record AdminTicketResponse(
        Long id,
        String subject,
        String category,
        String message,
        String ticketStatus,
        Instant createdAt,
        String studentFullName,
        String studentEmail,
        String studentNumber
) {}
