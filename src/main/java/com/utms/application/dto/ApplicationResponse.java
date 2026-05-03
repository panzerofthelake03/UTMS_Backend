package com.utms.application.dto;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        Long studentId,
        String status,
        String term,
        String applicationNote,
        Instant submittedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
