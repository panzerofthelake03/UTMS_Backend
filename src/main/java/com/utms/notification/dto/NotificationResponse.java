package com.utms.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long applicationId,
        String notificationType,
        String title,
        String message,
        boolean read,
        Instant readAt,
        Instant sentAt,
        Instant createdAt
) {}
