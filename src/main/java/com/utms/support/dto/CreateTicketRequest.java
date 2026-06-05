package com.utms.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 100) String subject,
        @NotBlank @Size(max = 50) String category,
        @NotBlank @Size(max = 1000) String message
) {}
