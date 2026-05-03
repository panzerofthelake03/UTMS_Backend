package com.utms.intibak;

import com.utms.common.api.ApiResponse;
import com.utms.intibak.dto.CourseExemptionRequest;
import com.utms.intibak.dto.CourseExemptionResponse;
import com.utms.intibak.dto.ExemptionDecisionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/intibak/{applicationId}/exemptions")
public class IntibakController {

    private final IntibakService intibakService;

    public IntibakController(IntibakService intibakService) {
        this.intibakService = intibakService;
    }

    /**
     * GET /api/intibak/{applicationId}/exemptions
     * Returns all course exemption entries for an application (split-view data).
     * Accessible by all authenticated staff + student owner.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseExemptionResponse>>> listExemptions(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success(intibakService.listExemptions(applicationId)));
    }

    /**
     * POST /api/intibak/{applicationId}/exemptions
     * Adds a course mapping entry (left: student course, right: target curriculum course).
     * Only staff roles can populate this.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_OIDB') or hasRole('ROLE_YDYO') or hasRole('ROLE_YGK') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CourseExemptionResponse>> addExemption(
            @PathVariable Long applicationId,
            @Valid @RequestBody CourseExemptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(intibakService.addExemption(applicationId, request)));
    }

    /**
     * PUT /api/intibak/{applicationId}/exemptions/{id}/decide
     * Records the exemption decision for a single course (EXEMPT | PARTIAL | REJECTED).
     * Requires a comment to enforce the manual-intervention audit trail.
     */
    @PutMapping("/{id}/decide")
    @PreAuthorize("hasRole('ROLE_OIDB') or hasRole('ROLE_YDYO') or hasRole('ROLE_YGK') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CourseExemptionResponse>> recordDecision(
            @PathVariable Long applicationId,
            @PathVariable Long id,
            @Valid @RequestBody ExemptionDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                intibakService.recordDecision(applicationId, id, request)));
    }
}
