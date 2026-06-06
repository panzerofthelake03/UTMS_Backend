package com.utms.oidb;

import com.utms.common.api.ApiResponse;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.oidb.dto.ForwardToYdyoRequest;
import com.utms.oidb.dto.RejectApplicationRequest;
import com.utms.student.dto.StudentProfileResponse;
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
@RequestMapping("/api/oidb")
@PreAuthorize("hasRole('ROLE_OIDB') or hasRole('ROLE_ADMIN')")
public class OidbController {

    private final OidbService oidbService;

    public OidbController(OidbService oidbService) {
        this.oidbService = oidbService;
    }

    /**
     * GET /api/oidb/applications
     * Lists all SUBMITTED and UNDER_OIDB_REVIEW applications.
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listPendingApplications() {
        return ResponseEntity.ok(ApiResponse.success(oidbService.listPendingApplications()));
    }

    /**
     * POST /api/oidb/applications/{id}/take-review
     * Marks a SUBMITTED application as UNDER_OIDB_REVIEW.
     */
    @PostMapping("/applications/{id}/take-review")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> takeReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(oidbService.takeReview(id)));
    }

    /**
     * POST /api/oidb/applications/{id}/forward-ydyo
     * Forwards an UNDER_OIDB_REVIEW application to YDYO.
     */
    @PostMapping("/applications/{id}/forward-ydyo")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> forwardToYdyo(
            @PathVariable Long id,
            @RequestBody(required = false) ForwardToYdyoRequest request) {
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(ApiResponse.success(oidbService.forwardToYdyo(id, note)));
    }

    /**
     * POST /api/oidb/applications/{id}/reject
     * Rejects a SUBMITTED or UNDER_OIDB_REVIEW application.
     */
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> rejectApplication(
            @PathVariable Long id,
            @RequestBody(required = false) RejectApplicationRequest request) {
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(ApiResponse.success(oidbService.rejectApplication(id, note)));
    }

    /**
     * GET /api/oidb/secondary-review
     * Returns applications in PENDING_DEAN_APPROVAL for ÖİDB secondary review.
     */
    @GetMapping("/secondary-review")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listSecondaryReview() {
        return ResponseEntity.ok(ApiResponse.success(oidbService.listPendingSecondaryReview()));
    }

    /**
     * POST /api/oidb/applications/{id}/secondary-reject
     * ÖİDB rejects a PENDING_DEAN_APPROVAL application after YGK review.
     */
    @PostMapping("/applications/{id}/secondary-reject")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> secondaryReject(
            @PathVariable Long id,
            @RequestBody(required = false) RejectApplicationRequest request) {
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(ApiResponse.success(oidbService.secondaryReject(id, note)));
    }

    /**
     * GET /api/oidb/results
     * UC 5.3 — Returns all finalized (ACCEPTED / REJECTED) applications.
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> getResults() {
        return ResponseEntity.ok(ApiResponse.success(oidbService.getResults()));
    }

    /**
     * GET /api/oidb/applications/{id}/student-profile
     * UC 3.2 — Returns full student identity details for the given application.
     */
    @GetMapping("/applications/{id}/student-profile")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getStudentProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(oidbService.getStudentProfile(id)));
    }
}
