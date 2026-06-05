package com.utms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * UC 1.3 steps 6-7: the student submits the 6-digit code for validation ("Verify & Continue").
 * Special Requirement #1: the code must be a 6-digit numeric OTP.
 */
public record VerifyResetCodeRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be exactly 6 digits") String code
) {
}
