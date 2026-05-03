package com.utms.application;

import com.utms.application.dto.ApplicationResponse;
import com.utms.application.dto.CreateApplicationRequest;
import com.utms.application.dto.StatusTimelineItemResponse;
import com.utms.application.dto.UpdateApplicationRequest;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.common.security.PermissionChecker;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.student.dto.StudentDashboardResponse;
import com.utms.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final StudentRepository studentRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final PermissionChecker permissionChecker;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationStatusHistoryRepository statusHistoryRepository,
                              StudentRepository studentRepository,
                              AuthenticatedUserService authenticatedUserService,
                              PermissionChecker permissionChecker) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.studentRepository = studentRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.permissionChecker = permissionChecker;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard() {
        Student student = getCurrentStudent();

        long totalApplications = applicationRepository.countByStudentId(student.getId());
        boolean hasDraft = applicationRepository.existsByStudentIdAndStatus(student.getId(), ApplicationStatus.DRAFT);
        String latestStatus = applicationRepository.findTopByStudentIdOrderByCreatedAtDesc(student.getId())
                .map(Application::getStatus)
                .orElse(null);

        return new StudentDashboardResponse(
                student.getStudentNumber(),
                student.getDepartment(),
                student.getFaculty(),
                student.getGpa(),
                totalApplications,
                latestStatus,
                hasDraft
        );
    }

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Student student = getCurrentStudent();

        Application application = new Application();
        application.setStudent(student);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setTerm(request.term());
        application.setApplicationNote(request.applicationNote());
        application = applicationRepository.save(application);

        saveTimeline(application, null, ApplicationStatus.DRAFT, currentUser, "Application draft created");
        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplication(Long applicationId, UpdateApplicationRequest request) {
        Application application = getOwnedApplication(applicationId);
        if (!permissionChecker.canEditApplication(application.getStudent().getUser(), application)) {
            throw new AccessDeniedException("Only the owner can edit DRAFT applications");
        }

        application.setTerm(request.term());
        application.setApplicationNote(request.applicationNote());

        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse submitApplication(Long applicationId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Application application = getOwnedApplication(applicationId);

        if (!permissionChecker.canSubmitApplication(application.getStudent().getUser(), application)) {
            throw new AccessDeniedException("Only the owner can submit DRAFT applications");
        }

        String fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(Instant.now());
        application = applicationRepository.save(application);

        saveTimeline(application, fromStatus, ApplicationStatus.SUBMITTED, currentUser, "Application submitted");
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMyApplications() {
        Student student = getCurrentStudent();
        return applicationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatusTimelineItemResponse> getTimeline(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!permissionChecker.canViewApplication(application.getStudent().getUser())) {
            throw new AccessDeniedException("You are not allowed to view this application timeline");
        }

        return statusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(applicationId)
                .stream()
                .map(item -> new StatusTimelineItemResponse(
                        item.getId(),
                        item.getFromStatus(),
                        item.getToStatus(),
                        item.getActorUser() == null ? null : item.getActorUser().getEmail(),
                        item.getNote(),
                        item.getChangedAt()))
                .toList();
    }

    private Application getOwnedApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!permissionChecker.isOwner(application.getStudent().getUser())) {
            throw new AccessDeniedException("You can only modify your own applications");
        }
        return application;
    }

    private Student getCurrentStudent() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Current user does not have a student profile"));
    }

    private void saveTimeline(Application application,
                              String fromStatus,
                              String toStatus,
                              User actorUser,
                              String note) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setActorUser(actorUser);
        history.setNote(note);
        history.setChangedAt(Instant.now());
        statusHistoryRepository.save(history);
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getStatus(),
                application.getTerm(),
                application.getApplicationNote(),
                application.getSubmittedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
