package com.utms.integration;

import com.utms.common.security.AuthenticatedUserService;
import com.utms.integration.dto.UbysAutofillResponse;
import com.utms.integration.dto.YoksisLookupResponse;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationService {

    private final UbysService ubysService;
    private final YoksisService yoksisService;
    private final StudentRepository studentRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public IntegrationService(UbysService ubysService,
                              YoksisService yoksisService,
                              StudentRepository studentRepository,
                              AuthenticatedUserService authenticatedUserService) {
        this.ubysService = ubysService;
        this.yoksisService = yoksisService;
        this.studentRepository = studentRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Fetches academic data from mock UBYS for the authenticated student
     * and updates their profile (GPA, department, faculty).
     */
    @Transactional
    public UbysAutofillResponse autofillFromUbys() {
        User currentUser = authenticatedUserService.getCurrentUser();
        Student student = studentRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new IllegalStateException("Student profile not found for current user"));

        UbysService.UbysStudentRecord record = ubysService.lookupByStudentNumber(student.getStudentNumber());

        boolean profileUpdated = false;
        if (record != null && !record.gpa().equals(java.math.BigDecimal.ZERO)) {
            student.setGpa(record.gpa());
            if (!"UNDECLARED".equals(record.department())) {
                student.setDepartment(record.department());
            }
            if (!"UNASSIGNED".equals(record.faculty())) {
                student.setFaculty(record.faculty());
            }
            studentRepository.save(student);
            profileUpdated = true;
        }

        return new UbysAutofillResponse(
                record.studentNumber(),
                record.department(),
                record.faculty(),
                record.gpa(),
                record.completedCredits(),
                record.completedCourses(),
                profileUpdated
        );
    }

    /**
     * Verifies enrollment of the authenticated student against the mock YÖKSİS registry.
     */
    public YoksisLookupResponse verifyEnrollment() {
        User currentUser = authenticatedUserService.getCurrentUser();
        Student student = studentRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new IllegalStateException("Student profile not found for current user"));

        YoksisService.YoksisStudentRecord record = yoksisService.verifyEnrollment(student.getStudentNumber());

        String message = record.enrollmentVerified()
                ? "Enrollment verified in YÖKSİS registry."
                : "Student number not found in YÖKSİS registry. Verify student number is correct.";

        return new YoksisLookupResponse(
                record.studentNumber(),
                record.firstName(),
                record.lastName(),
                record.nationality(),
                record.enrollmentVerified(),
                message
        );
    }
}
