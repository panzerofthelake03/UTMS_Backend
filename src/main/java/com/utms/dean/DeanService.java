package com.utms.dean;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.application.ApplicationStatus;
import com.utms.application.ApplicationStatusHistory;
import com.utms.application.ApplicationStatusHistoryRepository;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.notification.NotificationService;
import com.utms.student.Student;
import com.utms.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class DeanService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final NotificationService notificationService;

    public DeanService(ApplicationRepository applicationRepository,
                       ApplicationStatusHistoryRepository statusHistoryRepository,
                       AuthenticatedUserService authenticatedUserService,
                       NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.notificationService = notificationService;
    }

    /** UC 4.1 — Lists applications pending dean approval. */
    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listPendingApprovals() {
        return applicationRepository
                .findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING_DEAN_APPROVAL)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /** UC 4.1 — Dean approves: PENDING_DEAN_APPROVAL → ACCEPTED */
    @Transactional
    public AdminApplicationResponse approve(Long applicationId, String note) {
        Application application = findPending(applicationId);
        User actor = authenticatedUserService.getCurrentUser();

        String fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
        saveHistory(application, fromStatus, ApplicationStatus.ACCEPTED, actor,
                note != null && !note.isBlank() ? note : "Dean approved — application accepted");
        notificationService.createApplicationResultNotification(application, "ACCEPTED");
        return toAdminResponse(application);
    }

    /** UC 4.1 — Dean rejects: PENDING_DEAN_APPROVAL → REJECTED */
    @Transactional
    public AdminApplicationResponse reject(Long applicationId, String note) {
        Application application = findPending(applicationId);
        User actor = authenticatedUserService.getCurrentUser();

        String fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
        saveHistory(application, fromStatus, ApplicationStatus.REJECTED, actor,
                note != null && !note.isBlank() ? note : "Dean rejected — application not approved");
        notificationService.createApplicationResultNotification(application, "REJECTED");
        return toAdminResponse(application);
    }

    private Application findPending(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));
        if (!ApplicationStatus.PENDING_DEAN_APPROVAL.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application is not pending dean approval (current: " + application.getStatus() + ")");
        }
        return application;
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

    private AdminApplicationResponse toAdminResponse(Application app) {
        Student student = app.getStudent();
        User user = student.getUser();
        return new AdminApplicationResponse(
                app.getId(), app.getStatus(), app.getTerm(), app.getApplicationNote(),
                app.getSubmittedAt(), app.getCreatedAt(), app.getUpdatedAt(),
                student.getId(), student.getStudentNumber(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                student.getDepartment(), student.getFaculty(), student.getGpa(),
                student.getYksScore()
        );
    }
}
