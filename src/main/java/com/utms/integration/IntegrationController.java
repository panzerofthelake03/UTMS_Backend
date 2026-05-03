package com.utms.integration;

import com.utms.integration.dto.UbysAutofillResponse;
import com.utms.integration.dto.YoksisLookupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * GET /api/integration/ubys/autofill
     * Fetches GPA and transcript data from mock UBYS and updates the student's profile.
     */
    @GetMapping("/ubys/autofill")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<UbysAutofillResponse> autofillFromUbys() {
        return ResponseEntity.ok(integrationService.autofillFromUbys());
    }

    /**
     * GET /api/integration/yoksis/verify
     * Verifies the current student's enrollment in the mock YÖKSİS registry.
     */
    @GetMapping("/yoksis/verify")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<YoksisLookupResponse> verifyYoksis() {
        return ResponseEntity.ok(integrationService.verifyEnrollment());
    }
}
