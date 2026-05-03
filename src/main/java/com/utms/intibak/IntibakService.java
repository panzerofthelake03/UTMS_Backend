package com.utms.intibak;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.application.ApplicationStatus;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.intibak.dto.CourseExemptionRequest;
import com.utms.intibak.dto.CourseExemptionResponse;
import com.utms.intibak.dto.ExemptionDecisionRequest;
import com.utms.student.Student;
import com.utms.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class IntibakService {

    private static final Set<String> VALID_DECISIONS = Set.of("EXEMPT", "PARTIAL", "REJECTED");

    private final ApplicationRepository applicationRepository;
    private final CourseExemptionRepository exemptionRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public IntibakService(ApplicationRepository applicationRepository,
                          CourseExemptionRepository exemptionRepository,
                          AuthenticatedUserService authenticatedUserService) {
        this.applicationRepository = applicationRepository;
        this.exemptionRepository = exemptionRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Lists all ACCEPTED applications available for Intibak course-exemption review.
     */
    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listApplicationsForIntibak() {
        return applicationRepository
                .findByStatusOrderByCreatedAtAsc(ApplicationStatus.ACCEPTED)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * Returns all course exemption entries for an application (split-view data).
     */
    @Transactional(readOnly = true)
    public List<CourseExemptionResponse> listExemptions(Long applicationId) {
        verifyApplicationExists(applicationId);
        return exemptionRepository.findByApplicationIdOrderByStudentCourseCodeAsc(applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Adds a course mapping entry for an application.
     * Left side: student's course. Right side: target curriculum course.
     */
    @Transactional
    public CourseExemptionResponse addExemption(Long applicationId, CourseExemptionRequest request) {
        Application application = verifyApplicationExists(applicationId);

        CourseExemption exemption = new CourseExemption();
        exemption.setApplication(application);
        exemption.setStudentCourseCode(request.getStudentCourseCode());
        exemption.setStudentCourseName(request.getStudentCourseName());
        exemption.setStudentCourseCredits(request.getStudentCourseCredits());
        exemption.setStudentCourseGrade(request.getStudentCourseGrade());
        exemption.setTargetCourseCode(request.getTargetCourseCode());
        exemption.setTargetCourseName(request.getTargetCourseName());
        exemption.setTargetCourseCredits(request.getTargetCourseCredits());
        exemption.setDecision("PENDING");

        return toResponse(exemptionRepository.save(exemption));
    }

    /**
     * Records an exemption decision (EXEMPT | PARTIAL | REJECTED) for a single course entry.
     * Only staff roles can call this; enforced at controller level via @PreAuthorize.
     */
    @Transactional
    public CourseExemptionResponse recordDecision(Long applicationId, Long exemptionId,
                                                   ExemptionDecisionRequest request) {
        if (!VALID_DECISIONS.contains(request.getDecision())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid decision. Allowed values: EXEMPT, PARTIAL, REJECTED");
        }

        verifyApplicationExists(applicationId);

        CourseExemption exemption = exemptionRepository.findById(exemptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course exemption entry not found: " + exemptionId));

        if (!exemption.getApplication().getId().equals(applicationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Exemption entry does not belong to application: " + applicationId);
        }

        User actor = authenticatedUserService.getCurrentUser();
        exemption.setDecision(request.getDecision());
        exemption.setDecisionNote(request.getDecisionNote());
        exemption.setDecidedBy(actor);
        exemption.setDecidedAt(Instant.now());

        return toResponse(exemptionRepository.save(exemption));
    }

    private Application verifyApplicationExists(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));
    }

    private AdminApplicationResponse toAdminResponse(Application app) {
        Student student = app.getStudent();
        User user = student.getUser();
        return new AdminApplicationResponse(
                app.getId(),
                app.getStatus(),
                app.getTerm(),
                app.getApplicationNote(),
                app.getSubmittedAt(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                student.getId(),
                student.getStudentNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                student.getDepartment(),
                student.getFaculty(),
                student.getGpa());
    }

    private CourseExemptionResponse toResponse(CourseExemption e) {
        String decidedByEmail = e.getDecidedBy() != null ? e.getDecidedBy().getEmail() : null;
        return new CourseExemptionResponse(
                e.getId(),
                e.getApplication().getId(),
                e.getStudentCourseCode(),
                e.getStudentCourseName(),
                e.getStudentCourseCredits(),
                e.getStudentCourseGrade(),
                e.getTargetCourseCode(),
                e.getTargetCourseName(),
                e.getTargetCourseCredits(),
                e.getDecision(),
                e.getDecisionNote(),
                decidedByEmail,
                e.getDecidedAt(),
                e.getCreatedAt()
        );
    }
}
