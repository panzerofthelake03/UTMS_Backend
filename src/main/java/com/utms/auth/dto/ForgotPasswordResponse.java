package com.utms.auth.dto;

/**
 * UC 1.3 response to "Send Code".
 *
 * <p>{@code emailFound} indicates whether the given address is registered; the frontend uses this
 * to surface "email not registered" feedback instead of silently pretending to send a code.</p>
 *
 * <p>{@code expiresInSeconds} drives the "Code expires in mm:ss" countdown and is {@code null}
 * when {@code emailFound} is {@code false}.</p>
 *
 * <p>{@code devVerificationCode} is only populated when dev-expose-code is on (local development)
 * and is never populated in production.</p>
 */
public record ForgotPasswordResponse(
        String message,
        boolean emailFound,
        Long expiresInSeconds,
        String devVerificationCode
) {
}
