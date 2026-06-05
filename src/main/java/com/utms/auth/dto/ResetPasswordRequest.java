package com.utms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * UC 1.4: the student sets a new password using the temporary reset token from UC 1.3.
 *
 * <p>Password complexity (Special Requirement #1), match check (Alternative Course 4a) and the
 * last-3-passwords history check (4c) are enforced in the service so each rule can return the
 * specific message the use case requires (e.g. "Must contain uppercase").</p>
 */
public record ResetPasswordRequest(
        @NotBlank String resetToken,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {
}
