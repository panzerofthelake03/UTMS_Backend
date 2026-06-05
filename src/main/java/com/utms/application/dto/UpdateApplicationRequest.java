package com.utms.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateApplicationRequest(
        @NotBlank @Size(max = 50) String term,
        @Size(max = 5000) String applicationNote,
        @Size(max = 255) String targetDepartment,
        @Pattern(regexp = "^\\+?[0-9 \\-().]{7,20}$", message = "Invalid phone number format")
        @Size(max = 30) String phone,
        @Size(max = 1000) String address,
        @Size(max = 30) String englishProficiencyOption
) {
}
