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

    /** UC 2.2 individual review decisions */
    private static final Set<String> VALID_DECISIONS = Set.of("APPROVED", "EXAM_REQUIRED");
    /** UC 2.1 inline list decisions */
    private static final Set<String> VALID_LIST_DECISIONS = Set.of("PASS", "FAIL", "DOCUMENT_REQUIRED");

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
            .findByStatusInOrderByCreatedAtAsc(List.of(
                ApplicationStatus.UNDER_YDYO_REVIEW,
                ApplicationStatus.WAITING_EXAM_RESULT))
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * YDYO reviews the English proficiency document for an application.
     *
     * Decision APPROVED   → status moves to UNDER_YGK_REVIEW
    * Decision EXAM_REQUIRED → status moves to WAITING_EXAM_RESULT (waiting for exam outcome)
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

        if (!ApplicationStatus.UNDER_YDYO_REVIEW.equals(application.getStatus())
            && !ApplicationStatus.WAITING_EXAM_RESULT.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Application must be in UNDER_YDYO_REVIEW or WAITING_EXAM_RESULT status (current: " + application.getStatus() + ")");
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
            // Normalize YKS score (0–500) to language_score (0–100) for the composite formula
            if (evaluation.getLanguageScore() == null && application.getStudent().getYksScore() != null) {
                evaluation.setLanguageScore(application.getStudent().getYksScore()
                        .divide(new java.math.BigDecimal("5"), 2, java.math.RoundingMode.HALF_UP));
                evaluationRepository.save(evaluation);
            }
        } else {
            toStatus = ApplicationStatus.WAITING_EXAM_RESULT;
            historyNote = "Language exam required — waiting for exam result";
        }

        application.setStatus(toStatus);
        applicationRepository.save(application);

        saveHistory(application, fromStatus, toStatus, actor,
                request.getReviewerNote() != null ? request.getReviewerNote() : historyNote);

        return toAdminResponse(application);
    }

    /**
     * UC 2.1 - Set inline decision for a single application (PASS / FAIL / DOCUMENT_REQUIRED).
     * Stores the decision in the evaluation row without immediately transitioning status.
     * Status transitions happen when "Send to OIDB" is called.
     */
    @Transactional
    public AdminApplicationResponse setInlineDecision(Long applicationId, String decision) {
        if (!VALID_LIST_DECISIONS.contains(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid decision. Allowed: PASS, FAIL, DOCUMENT_REQUIRED");
        }

        User actor = authenticatedUserService.getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        if (!ApplicationStatus.UNDER_YDYO_REVIEW.equals(application.getStatus())
                && !ApplicationStatus.WAITING_EXAM_RESULT.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application is not in YDYO review state (current: " + application.getStatus() + ")");
        }

        Evaluation evaluation = evaluationRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    Evaluation e = new Evaluation();
                    e.setApplication(application);
                    e.setDecision("PENDING");
                    return e;
                });
        evaluation.setYdyoDecision(decision);
        evaluation.setYdyoReviewer(actor);
        evaluationRepository.save(evaluation);

        return toAdminResponse(application);
    }

    /**
     * UC 2.1 - Send all YDYO-decided applications to OIDB.
     * PASS → UNDER_YGK_REVIEW
     * FAIL → REJECTED
     * DOCUMENT_REQUIRED → WAITING_EXAM_RESULT
     */
    @Transactional
    public int sendToOidb() {
        User actor = authenticatedUserService.getCurrentUser();
        List<Application> apps = applicationRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(ApplicationStatus.UNDER_YDYO_REVIEW, ApplicationStatus.WAITING_EXAM_RESULT));

        int processed = 0;
        for (Application app : apps) {
            evaluationRepository.findByApplicationId(app.getId()).ifPresent(eval -> {
                if (eval.getYdyoDecision() == null) return;
                String fromStatus = app.getStatus();
                String toStatus;
                String note;
                switch (eval.getYdyoDecision()) {
                    case "PASS" -> {
                        toStatus = ApplicationStatus.UNDER_YGK_REVIEW;
                        note = "YDYO: Pass — forwarded to YGK";
                        if (eval.getLanguageScore() == null && app.getStudent().getYksScore() != null) {
                            eval.setLanguageScore(app.getStudent().getYksScore()
                                    .divide(new java.math.BigDecimal("5"), 2, java.math.RoundingMode.HALF_UP));
                            evaluationRepository.save(eval);
                        }
                    }
                    case "FAIL" -> { toStatus = ApplicationStatus.REJECTED; note = "YDYO: Fail — application rejected"; }
                    case "DOCUMENT_REQUIRED" -> { toStatus = ApplicationStatus.WAITING_EXAM_RESULT; note = "YDYO: Document required — waiting exam"; }
                    default -> { return; }
                }
                app.setStatus(toStatus);
                applicationRepository.save(app);
                saveHistory(app, fromStatus, toStatus, actor, note);
            });
            processed++;
        }
        return processed;
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
                student.getGpa(),
                student.getYksScore()
        );
    }
}
