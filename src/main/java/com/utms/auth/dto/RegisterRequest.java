package com.utms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Size(max = 100) String nationality,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Pattern(regexp = "TC_ID|PASSPORT", message = "must be TC_ID or PASSPORT") String identityDocumentType,
        @Size(max = 20) String tcIdentityNumber,
        @Size(max = 30) String identitySerialNo,
        @Size(max = 30) String passportNumber,
        LocalDate passportExpirationDate,
        @NotBlank @Size(max = 255) String currentProgram,
        @NotBlank @Size(max = 255) String currentUniversity
) {}
