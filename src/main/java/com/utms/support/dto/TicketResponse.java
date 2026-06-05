package com.utms.support.dto;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String subject,
        String category,
        String message,
        String ticketStatus,
        Instant createdAt
) {}
