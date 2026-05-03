package com.utms.auth.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
