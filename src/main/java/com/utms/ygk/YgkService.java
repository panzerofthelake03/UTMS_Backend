package com.utms.ygk;

import com.utms.application.Application;
import com.utms.application.ApplicationRepository;
import com.utms.application.ApplicationStatus;
import com.utms.application.ApplicationStatusHistory;
import com.utms.application.ApplicationStatusHistoryRepository;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.common.security.AuthenticatedUserService;
import com.utms.student.Student;
import com.utms.user.User;
import com.utms.ygk.dto.EvaluationRequest;
import com.utms.ygk.dto.EvaluationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class YgkService {

    private static final Set<String> VALID_DECISIONS = Set.of("ACCEPTED", "REJECTED");

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final EvaluationRepository evaluationRepository;
    private final CompositeScoreService compositeScoreService;
    private final AuthenticatedUserService authenticatedUserService;

    public YgkService(ApplicationRepository applicationRepository,
                      ApplicationStatusHistoryRepository statusHistoryRepository,
                      EvaluationRepository evaluationRepository,
                      CompositeScoreService compositeScoreService,
                      AuthenticatedUserService authenticatedUserService) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.evaluationRepository = evaluationRepository;
        this.compositeScoreService = compositeScoreService;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Lists all applications currently in UNDER_YGK_REVIEW status.
     */
    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listApplicationsForEvaluation() {
        return applicationRepository
                .findByStatusOrderByCreatedAtAsc(ApplicationStatus.UNDER_YGK_REVIEW)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * Returns the evaluation record for a specific application.
     */
    @Transactional(readOnly = true)
    public EvaluationResponse getEvaluation(Long applicationId) {
        Evaluation evaluation = evaluationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No evaluation record found for application: " + applicationId));
        return toResponse(evaluation);
    }

    /**
     * YGK evaluator submits the composite score and final decision.
     * Computes composite score from GPA + language score + manual adjustment.
     * Decision ACCEPTED / REJECTED updates the application status accordingly.
     */
    @Transactional
    public EvaluationResponse submitEvaluation(Long applicationId, EvaluationRequest request) {
        if (!VALID_DECISIONS.contains(request.getDecision())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid decision. Allowed values: ACCEPTED, REJECTED");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found: " + applicationId));

        if (!ApplicationStatus.UNDER_YGK_REVIEW.equals(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application must be in UNDER_YGK_REVIEW status (current: " + application.getStatus() + ")");
        }

        User actor = authenticatedUserService.getCurrentUser();

        // Retrieve existing evaluation (created by YDYO step) or create new
        Evaluation evaluation = evaluationRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    Evaluation e = new Evaluation();
                    e.setApplication(application);
                    return e;
                });

        // Compute composite score using student GPA + submitted language score
        BigDecimal gpa = application.getStudent().getGpa() != null
                ? application.getStudent().getGpa()
                : BigDecimal.ZERO;

        BigDecimal compositeScore = compositeScoreService.compute(
                gpa,
                request.getLanguageScore(),
                request.getAdjustment()
        );

        evaluation.setCompositeScore(compositeScore);
        evaluation.setEvaluatorNote(request.getEvaluatorNote());
        evaluation.setDecision(request.getDecision());
        evaluationRepository.save(evaluation);

        // Transition application status
        String fromStatus = application.getStatus();
        String toStatus = "ACCEPTED".equals(request.getDecision())
                ? ApplicationStatus.ACCEPTED
                : ApplicationStatus.REJECTED;

        application.setStatus(toStatus);
        applicationRepository.save(application);

        String historyNote = String.format("YGK evaluation: score=%.2f, decision=%s",
                compositeScore, request.getDecision());
        if (request.getEvaluatorNote() != null && !request.getEvaluatorNote().isBlank()) {
            historyNote = request.getEvaluatorNote();
        }
        saveHistory(application, fromStatus, toStatus, actor, historyNote);

        return toResponse(evaluation);
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

    private EvaluationResponse toResponse(Evaluation e) {
        return new EvaluationResponse(
                e.getId(),
                e.getApplication().getId(),
                e.getCompositeScore(),
                e.getEvaluatorNote(),
                e.getDecision(),
                e.getYdyoDecision(),
                e.getYdyoNote(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
