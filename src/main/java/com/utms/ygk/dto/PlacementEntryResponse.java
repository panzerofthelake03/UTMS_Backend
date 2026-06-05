package com.utms.ygk.dto;

import java.math.BigDecimal;

public record PlacementEntryResponse(
        int rank,
        Long applicationId,
        String studentName,
        String studentNumber,
        String department,
        String faculty,
        String term,
        BigDecimal compositeScore,
        String status
) {}
