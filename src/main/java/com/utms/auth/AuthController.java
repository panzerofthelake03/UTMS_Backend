package com.utms.auth;

import com.utms.auth.dto.AuthResponse;
import com.utms.auth.dto.CaptchaChallengeResponse;
import com.utms.auth.dto.LoginRequest;
import com.utms.auth.dto.RegisterCancelRequest;
import com.utms.auth.dto.RefreshTokenRequest;
import com.utms.auth.dto.RefreshTokenResponse;
import com.utms.auth.dto.RegisterStartRequest;
import com.utms.auth.dto.RegisterStartResponse;
import com.utms.auth.dto.RegisterVerifyRequest;
import com.utms.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha/challenge")
    public ResponseEntity<ApiResponse<CaptchaChallengeResponse>> captchaChallenge() {
        CaptchaChallengeResponse response = authService.getCaptchaChallenge();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register/start")
    public ResponseEntity<ApiResponse<RegisterStartResponse>> registerStart(@Valid @RequestBody RegisterStartRequest request) {
        RegisterStartResponse response = authService.registerStart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> registerVerify(@Valid @RequestBody RegisterVerifyRequest request) {
        AuthResponse response = authService.verifyRegistration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/register/cancel")
    public ResponseEntity<ApiResponse<Void>> registerCancel(@Valid @RequestBody RegisterCancelRequest request) {
        authService.cancelRegistration(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
