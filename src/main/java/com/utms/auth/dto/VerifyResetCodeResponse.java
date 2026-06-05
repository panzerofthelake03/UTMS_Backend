package com.utms.auth.dto;

/**
 * UC 1.3 POST-2: on successful identity verification the student receives a temporary reset token
 * to carry into the Reset Password page (UC 1.4). {@code expiresInSeconds} reflects the reset
 * session inactivity timeout (5 minutes).
 */
public record VerifyResetCodeResponse(
        String resetToken,
        Long expiresInSeconds
) {
}
