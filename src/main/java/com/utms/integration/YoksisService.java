package com.utms.integration;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock YÖKSİS (Yükseköğretim Bilgi Sistemi) integration service.
 * Returns simulated national higher-education registry data by student number.
 */
@Service
public class YoksisService {

    private static final Map<String, YoksisStudentRecord> MOCK_REGISTRY = new HashMap<>();

    static {
        MOCK_REGISTRY.put("20210001", new YoksisStudentRecord(
                "20210001", "Ali", "Yılmaz",
                "12345678901", "TR", true
        ));
        MOCK_REGISTRY.put("20210002", new YoksisStudentRecord(
                "20210002", "Ayşe", "Kaya",
                "98765432109", "TR", true
        ));
        MOCK_REGISTRY.put("20210003", new YoksisStudentRecord(
                "20210003", "Mehmet", "Demir",
                "11122233344", "TR", true
        ));
    }

    /**
     * Verifies student enrollment status in the national registry.
     *
     * @param studentNumber the student's university ID
     * @return YoksisStudentRecord, or an unverified placeholder for unknown IDs
     */
    public YoksisStudentRecord verifyEnrollment(String studentNumber) {
        return MOCK_REGISTRY.getOrDefault(studentNumber, new YoksisStudentRecord(
                studentNumber, "UNKNOWN", "UNKNOWN",
                "00000000000", "UNKNOWN", false
        ));
    }

    public record YoksisStudentRecord(
            String studentNumber,
            String firstName,
            String lastName,
            String nationalId,
            String nationality,
            boolean enrollmentVerified
    ) {}
}
