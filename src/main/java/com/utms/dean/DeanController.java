package com.utms.dean;

import com.utms.common.api.ApiResponse;
import com.utms.common.dto.AdminApplicationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dean")
@PreAuthorize("hasRole('ROLE_DEAN') or hasRole('ROLE_ADMIN')")
public class DeanController {

    private final DeanService deanService;

    public DeanController(DeanService deanService) {
        this.deanService = deanService;
    }

    /** GET /api/dean/applications — UC 4.1: list applications pending dean approval */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<AdminApplicationResponse>>> listPendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success(deanService.listPendingApprovals()));
    }

    /** POST /api/dean/applications/{id}/approve — UC 4.1: dean approves */
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(ApiResponse.success(deanService.approve(id, note)));
    }

    /** POST /api/dean/applications/{id}/reject — UC 4.1: dean rejects */
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<AdminApplicationResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(ApiResponse.success(deanService.reject(id, note)));
    }
}
