package com.utms.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterCancelRequest(@NotBlank String verificationSessionId) {
}
