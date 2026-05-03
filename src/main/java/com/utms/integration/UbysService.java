package com.utms.integration;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock UBYS (Üniversite Bilgi Yönetim Sistemi) integration service.
 * Returns simulated transcript and GPA data by student number.
 */
@Service
public class UbysService {

    // Simulated student registry keyed by student number
    private static final Map<String, UbysStudentRecord> MOCK_REGISTRY = new HashMap<>();

    static {
        MOCK_REGISTRY.put("20210001", new UbysStudentRecord(
                "20210001", "Computer Engineering", "Faculty of Engineering",
                new BigDecimal("3.45"), 120,
                List.of("CENG101-A", "CENG201-B+", "MATH101-A", "PHYS101-B", "ENG101-A")
        ));
        MOCK_REGISTRY.put("20210002", new UbysStudentRecord(
                "20210002", "International Relations", "Faculty of Economics and Administrative Sciences",
                new BigDecimal("3.78"), 110,
                List.of("IR101-A", "IR201-A", "ECON101-B+", "POL101-A", "ENG201-A")
        ));
        MOCK_REGISTRY.put("20210003", new UbysStudentRecord(
                "20210003", "Mechanical Engineering", "Faculty of Engineering",
                new BigDecimal("2.91"), 95,
                List.of("ME101-B", "ME201-C+", "MATH201-B", "PHYS201-B+", "ENG101-B+")
        ));
    }

    /**
     * Looks up student data from the mock UBYS by student number.
     *
     * @param studentNumber the student's university ID number
     * @return UbysStudentRecord if found, otherwise null
     */
    public UbysStudentRecord lookupByStudentNumber(String studentNumber) {
        // Simulate a brief lookup (no actual HTTP call in mock)
        return MOCK_REGISTRY.getOrDefault(studentNumber, buildGenericRecord(studentNumber));
    }

    private UbysStudentRecord buildGenericRecord(String studentNumber) {
        // For unknown student numbers, return a generic placeholder record
        return new UbysStudentRecord(
                studentNumber, "UNDECLARED", "UNASSIGNED",
                new BigDecimal("0.00"), 0,
                List.of()
        );
    }

    public record UbysStudentRecord(
            String studentNumber,
            String department,
            String faculty,
            BigDecimal gpa,
            int completedCredits,
            List<String> completedCourses
    ) {}
}
