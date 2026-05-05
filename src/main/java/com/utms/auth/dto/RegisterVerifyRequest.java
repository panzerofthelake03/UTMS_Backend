package com.utms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterVerifyRequest(
        @NotBlank String verificationSessionId,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be exactly 6 digits") String code
) {
}
