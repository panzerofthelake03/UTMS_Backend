package com.utms.intibak;

import com.utms.common.api.ApiResponse;
import com.utms.common.dto.AdminApplicationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides the Intibak staff queue: lists all ACCEPTED applications
 * that are eligible for course-exemption review.
 */
@RestController
@RequestMapping("/api/intibak/applications")
@PreAuthorize("hasRole('ROLE_INTIBAK') or hasRole('ROLE_ADMIN')")
public class IntibakQueueController {

    private final IntibakService intibakService;

    public IntibakQueueController(IntibakService intibakService) {
        this.intibakService = intibakService;
    }

    /**
     * GET /api/intibak/applications
     * Returns all ACCEPTED applications for Intibak review.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listApplications() {
        return ResponseEntity.ok(ApiResponse.success(intibakService.listApplicationsForIntibak()));
    }
}
