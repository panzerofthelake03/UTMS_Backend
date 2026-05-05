package com.utms.student;

import com.utms.common.security.AuthenticatedUserService;
import com.utms.student.dto.StudentProfileResponse;
import com.utms.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public StudentService(StudentRepository studentRepository,
                          AuthenticatedUserService authenticatedUserService) {
        this.studentRepository = studentRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getCurrentStudentProfile() {
        User currentUser = authenticatedUserService.getCurrentUser();

        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Current user does not have a student profile"));

        return new StudentProfileResponse(
                buildFullName(currentUser.getFirstName(), currentUser.getLastName()),
                currentUser.getEmail(),
                student.getNationality(),
                student.getIdentityDocumentType(),
                student.getTcIdentityNumber(),
                student.getPassportNumber(),
                student.getDateOfBirth(),
                student.getIdentitySerialNo(),
                student.getPassportExpirationDate(),
                student.getCurrentProgram(),
                student.getCurrentUniversity()
        );
    }

    private String buildFullName(String firstName, String lastName) {
        String safeFirst = firstName == null ? "" : firstName.trim();
        String safeLast = lastName == null ? "" : lastName.trim();
        return (safeFirst + " " + safeLast).trim();
    }
}
