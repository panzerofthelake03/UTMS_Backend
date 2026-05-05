package com.utms.auth.dto;

public record CaptchaChallengeResponse(
        String captchaId,
        String prompt,
        Long expiresInSeconds
) {
}
