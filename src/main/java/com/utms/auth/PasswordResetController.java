package com.utms.auth;

import com.utms.auth.dto.ForgotPasswordRequest;
import com.utms.auth.dto.ForgotPasswordResponse;
import com.utms.auth.dto.ResetPasswordRequest;
import com.utms.auth.dto.VerifyResetCodeRequest;
import com.utms.auth.dto.VerifyResetCodeResponse;
import com.utms.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Password-recovery endpoints (UC 1.3 + UC 1.4). All endpoints are public (a not-logged-in
 * student) and are whitelisted in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /** UC 1.3 step 3 - "Send Code". Also serves UC 1.3 7b step 3 "Resend Code". */
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgot(
            @Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** UC 1.3 steps 6-7 - "Verify & Continue". */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyResetCodeResponse>> verify(
            @Valid @RequestBody VerifyResetCodeRequest request) {
        VerifyResetCodeResponse response = passwordResetService.verifyCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** UC 1.4 - "Set Password". */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> reset(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
