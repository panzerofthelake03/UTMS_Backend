package com.utms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * UC 1.3 step 3 - the student submits their email to receive a reset OTP ("Send Code").
 */
public record ForgotPasswordRequest(
        @NotBlank @Email String email
) {
}
