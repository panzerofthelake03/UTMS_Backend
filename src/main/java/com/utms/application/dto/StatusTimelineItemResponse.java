package com.utms.application.dto;

import java.time.Instant;

public record StatusTimelineItemResponse(
        Long id,
        String fromStatus,
        String toStatus,
        String actorEmail,
        String note,
        Instant changedAt
) {
}
