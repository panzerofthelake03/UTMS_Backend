package com.utms.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotBlank @Size(max = 50) String term,
        @Size(max = 5000) String applicationNote,
        @Size(max = 255) String targetDepartment,
        /** UC 1.6 SR-5: +90... format */
        @Pattern(regexp = "^\\+?[0-9 \\-().]{7,20}$", message = "Invalid phone number format")
        @Size(max = 30) String phone,
        @Size(max = 1000) String address,
        /** "DOCUMENT" | "YDYO_EXAM" */
        @Size(max = 30) String englishProficiencyOption
) {
}
