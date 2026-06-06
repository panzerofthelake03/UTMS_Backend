package com.utms.support;

import com.utms.common.api.ApiResponse;
import com.utms.support.dto.AdminTicketResponse;
import com.utms.support.dto.CreateTicketRequest;
import com.utms.support.dto.TicketResponse;
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
import java.util.Map;

/**
 * UC 1.7 - Submit a support message to OIDB.
 */
@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping("/tickets")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ROLE_OIDB') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminTicketResponse>>> listTickets() {
        return ResponseEntity.ok(ApiResponse.success(supportTicketService.listAllTickets()));
    }

    @PutMapping("/tickets/{id}/status")
    @PreAuthorize("hasRole('ROLE_OIDB') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminTicketResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(ApiResponse.success(supportTicketService.updateTicketStatus(id, status)));
    }
}
