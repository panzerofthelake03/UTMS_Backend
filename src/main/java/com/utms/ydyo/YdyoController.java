package com.utms.ydyo;

import com.utms.common.api.ApiResponse;
import com.utms.common.dto.AdminApplicationResponse;
import com.utms.ydyo.dto.EnglishReviewRequest;
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
@RequestMapping("/api/ydyo")
@PreAuthorize("hasRole('ROLE_YDYO') or hasRole('ROLE_ADMIN')")
public class YdyoController {

    private final YdyoService ydyoService;

    public YdyoController(YdyoService ydyoService) {
        this.ydyoService = ydyoService;
    }

    /**
     * GET /api/ydyo/applications
     * Lists all applications in UNDER_YDYO_REVIEW status.
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listApplications() {
        return ResponseEntity.ok(ApiResponse.success(ydyoService.listApplicationsUnderReview()));
    }

    /**
     * POST /api/ydyo/applications/{id}/english-review
     * Records the YDYO English document review decision.
     * Decision: APPROVED | EXAM_REQUIRED
     */
    @PostMapping("/applications/{id}/english-review")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> reviewEnglish(
            @PathVariable Long id,
            @Valid @RequestBody EnglishReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ydyoService.reviewEnglishDocument(id, request)));
    }
}
