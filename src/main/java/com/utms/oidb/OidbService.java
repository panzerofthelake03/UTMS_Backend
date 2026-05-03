package com.utms.oidb;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.application.ApplicationStatus;
import com.utms.application.ApplicationStatusHistory;
import com.utms.application.ApplicationStatusHistoryRepository;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.student.Student;
import com.utms.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class OidbService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public OidbService(ApplicationRepository applicationRepository,
                       ApplicationStatusHistoryRepository statusHistoryRepository,
                       AuthenticatedUserService authenticatedUserService) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Lists all applications currently awaiting OIDB action:
     * SUBMITTED (not yet reviewed) and UNDER_OIDB_REVIEW.
     */
    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listPendingApplications() {
        return applicationRepository
                .findByStatusInOrderByCreatedAtAsc(
                        List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_OIDB_REVIEW))
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * OIDB starts reviewing a SUBMITTED application.
     * Transition: SUBMITTED → UNDER_OIDB_REVIEW
     */
    @Transactional
    public AdminApplicationResponse takeReview(Long applicationId) {
        User actor = authenticatedUserService.getCurrentUser();
        Application application = findApplication(applicationId);

        if (!ApplicationStatus.SUBMITTED.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application must be in SUBMITTED status to start OIDB review (current: " + application.getStatus() + ")");
        }

        String fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.UNDER_OIDB_REVIEW);
        applicationRepository.save(application);
        saveHistory(application, fromStatus, ApplicationStatus.UNDER_OIDB_REVIEW, actor, "OIDB review started");

        return toAdminResponse(application);
    }

    /**
     * OIDB forwards an application to YDYO for English document review.
     * Transition: UNDER_OIDB_REVIEW → UNDER_YDYO_REVIEW
     */
    @Transactional
    public AdminApplicationResponse forwardToYdyo(Long applicationId, String note) {
        User actor = authenticatedUserService.getCurrentUser();
        Application application = findApplication(applicationId);

        if (!ApplicationStatus.UNDER_OIDB_REVIEW.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application must be in UNDER_OIDB_REVIEW status to forward to YDYO (current: " + application.getStatus() + ")");
        }

        String fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.UNDER_YDYO_REVIEW);
        applicationRepository.save(application);

        String historyNote = note != null && !note.isBlank() ? note : "Forwarded to YDYO for English document review";
        saveHistory(application, fromStatus, ApplicationStatus.UNDER_YDYO_REVIEW, actor, historyNote);

        return toAdminResponse(application);
    }

    private Application findApplication(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));
    }

    private void saveHistory(Application application, String fromStatus, String toStatus,
                             User actor, String note) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setActorUser(actor);
        history.setNote(note);
        history.setChangedAt(Instant.now());
        statusHistoryRepository.save(history);
    }

    AdminApplicationResponse toAdminResponse(Application app) {
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
                student.getGpa()
        );
    }
}
