package com.utms.ydyo.dto;

import jakarta.validation.constraints.NotBlank;

public record InlineDecisionRequest(
        @NotBlank String decision
) {}
