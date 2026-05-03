package com.utms.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotBlank @Size(max = 50) String term,
        @Size(max = 5000) String applicationNote
) {
}
