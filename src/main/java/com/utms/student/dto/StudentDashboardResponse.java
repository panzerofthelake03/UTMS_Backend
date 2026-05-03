package com.utms.student.dto;

import java.math.BigDecimal;

public record StudentDashboardResponse(
        String studentNumber,
        String department,
        String faculty,
        BigDecimal gpa,
        Long totalApplications,
        String latestApplicationStatus,
        boolean hasDraftApplication
) {
}
