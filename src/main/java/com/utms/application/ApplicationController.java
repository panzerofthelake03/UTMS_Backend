package com.utms.application;

import com.utms.application.dto.ApplicationResponse;
import com.utms.application.dto.CreateApplicationRequest;
import com.utms.application.dto.StatusTimelineItemResponse;
import com.utms.application.dto.UpdateApplicationRequest;
import com.utms.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submit(@PathVariable Long id) {
        ApplicationResponse response = applicationService.submitApplication(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        applicationService.deleteDraftApplication(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> myApplications() {
        return ResponseEntity.ok(ApiResponse.success(applicationService.listMyApplications()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApplicationResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplication(id)));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<StatusTimelineItemResponse>>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getTimeline(id)));
    }
}
