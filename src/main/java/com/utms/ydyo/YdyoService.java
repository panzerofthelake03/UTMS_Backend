package com.utms.ydyo;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.application.ApplicationStatus;
import com.utms.application.ApplicationStatusHistory;
import com.utms.application.ApplicationStatusHistoryRepository;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.student.Student;
import com.utms.user.User;
import com.utms.ydyo.dto.EnglishReviewRequest;
import com.utms.ygk.Evaluation;
import com.utms.ygk.EvaluationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class YdyoService {

    private static final Set<String> VALID_DECISIONS = Set.of("APPROVED", "EXAM_REQUIRED");

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final EvaluationRepository evaluationRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public YdyoService(ApplicationRepository applicationRepository,
                       ApplicationStatusHistoryRepository statusHistoryRepository,
                       EvaluationRepository evaluationRepository,
                       AuthenticatedUserService authenticatedUserService) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.evaluationRepository = evaluationRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Lists all applications currently in UNDER_YDYO_REVIEW status.
     */
    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listApplicationsUnderReview() {
        return applicationRepository
                .findByStatusOrderByCreatedAtAsc(ApplicationStatus.UNDER_YDYO_REVIEW)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * YDYO reviews the English proficiency document for an application.
     *
     * Decision APPROVED   → status moves to UNDER_YGK_REVIEW
     * Decision EXAM_REQUIRED → status stays at UNDER_YDYO_REVIEW (student needs language exam)
     */
    @Transactional
    public AdminApplicationResponse reviewEnglishDocument(Long applicationId, EnglishReviewRequest request) {
        if (!VALID_DECISIONS.contains(request.getDecision())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid decision. Allowed values: APPROVED, EXAM_REQUIRED");
        }

        User actor = authenticatedUserService.getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        if (!ApplicationStatus.UNDER_YDYO_REVIEW.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application must be in UNDER_YDYO_REVIEW status (current: " + application.getStatus() + ")");
        }

        // Persist YDYO decision in the evaluations row (create if absent)
        Evaluation evaluation = evaluationRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    Evaluation e = new Evaluation();
                    e.setApplication(application);
                    e.setDecision("PENDING");
                    return e;
                });
        evaluation.setYdyoDecision(request.getDecision());
        evaluation.setYdyoNote(request.getReviewerNote());
        evaluation.setYdyoReviewer(actor);
        evaluationRepository.save(evaluation);

        String fromStatus = application.getStatus();
        String toStatus;
        String historyNote;

        if ("APPROVED".equals(request.getDecision())) {
            toStatus = ApplicationStatus.UNDER_YGK_REVIEW;
            historyNote = "English document approved by YDYO — forwarded to YGK";
        } else {
            toStatus = ApplicationStatus.UNDER_YDYO_REVIEW;
            historyNote = "Language exam required — application remains under YDYO review";
        }

        application.setStatus(toStatus);
        applicationRepository.save(application);

        saveHistory(application, fromStatus, toStatus, actor,
                request.getReviewerNote() != null ? request.getReviewerNote() : historyNote);

        return toAdminResponse(application);
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
