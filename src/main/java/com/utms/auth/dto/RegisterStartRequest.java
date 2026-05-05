package com.utms.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record RegisterStartRequest(
        @NotBlank String captchaId,
        @NotBlank String captchaAnswer,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String nationality,
        LocalDate dateOfBirth,
        @NotBlank String identityDocumentType,
        String tcIdentityNumber,
        String identitySerialNo,
        String passportNumber,
        LocalDate passportExpirationDate,
        @NotBlank String currentProgram,
        @NotBlank String currentUniversity
) {
}
