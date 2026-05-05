package com.utms.auth.dto;

public record RegisterStartResponse(
        String verificationSessionId,
        String maskedEmail,
        Long expiresInSeconds,
        String devVerificationCode
) {
}
