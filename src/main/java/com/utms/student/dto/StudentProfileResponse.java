package com.utms.student.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        String fullName,
        String email,
        String nationality,
        String identityDocumentType,
        String tcIdentityNumber,
        String passportNumber,
        LocalDate dateOfBirth,
        String identitySerialNo,
        LocalDate passportExpirationDate,
        String currentProgram,
        String currentUniversity
) {
}
