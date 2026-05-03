package com.utms.ygk;

import com.utms.common.api.ApiResponse;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.ygk.dto.EvaluationRequest;
import com.utms.ygk.dto.EvaluationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ygk")
@PreAuthorize("hasRole('ROLE_YGK') or hasRole('ROLE_ADMIN')")
public class YgkController {

    private final YgkService ygkService;

    public YgkController(YgkService ygkService) {
        this.ygkService = ygkService;
    }

    /**
     * GET /api/ygk/applications
     * Lists all applications in UNDER_YGK_REVIEW status.
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listApplications() {
        return ResponseEntity.ok(ApiResponse.success(ygkService.listApplicationsForEvaluation()));
    }

    /**
     * GET /api/ygk/applications/{id}/evaluation
     * Retrieves the evaluation record for an application.
     */
    @GetMapping("/applications/{id}/evaluation")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getEvaluation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ygkService.getEvaluation(id)));
    }

    /**
     * POST /api/ygk/applications/{id}/evaluate
     * Submits composite score and final decision (ACCEPTED | REJECTED).
     */
    @PostMapping("/applications/{id}/evaluate")
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluate(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ygkService.submitEvaluation(id, request)));
    }
}
