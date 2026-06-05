package com.utms.auth;

import com.utms.auth.dto.ForgotPasswordRequest;
import com.utms.auth.dto.ForgotPasswordResponse;
import com.utms.auth.dto.ResetPasswordRequest;
import com.utms.auth.dto.VerifyResetCodeRequest;
import com.utms.auth.dto.VerifyResetCodeResponse;
import com.utms.user.PasswordHistory;
import com.utms.user.PasswordHistoryRepository;
import com.utms.user.User;
import com.utms.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implements the password-recovery flow.
 *
 * <p>UC 1.3 (Verify Identity for Password Reset): {@link #forgotPassword} issues a 6-digit OTP;
 * {@link #verifyCode} validates it and hands back a temporary reset token.</p>
 *
 * <p>UC 1.4 (Reset Password): {@link #resetPassword} consumes the reset token, validates the new
 * password (complexity, match, history) and updates the stored credential.</p>
 *
 * <p>The flow never reveals whether an email is registered (Alternative Course 4b - email
 * enumeration protection): {@link #forgotPassword} always returns the same generic response.</p>
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final String GENERIC_SEND_MESSAGE = "If this email is registered, a code has been sent.";

    /** Account lock window, kept in sync with {@code AuthService.LOCK_TIME_DURATION_MINUTES}. */
    private static final long LOCK_TIME_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository resetRequestRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.password-reset.code-expiration-seconds:180}")
    private long codeExpirationSeconds;

    @Value("${app.auth.password-reset.max-verify-attempts:5}")
    private int maxVerifyAttempts;

    @Value("${app.auth.password-reset.resend-window-minutes:15}")
    private long resendWindowMinutes;

    @Value("${app.auth.password-reset.resend-max-requests:3}")
    private int resendMaxRequests;

    @Value("${app.auth.password-reset.reset-token-timeout-seconds:300}")
    private long resetTokenTimeoutSeconds;

    @Value("${app.auth.password-reset.history-count:3}")
    private int historyCount;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetRequestRepository resetRequestRepository,
                                PasswordHistoryRepository passwordHistoryRepository,
                                PasswordResetEmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resetRequestRepository = resetRequestRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------------------------------------
    // UC 1.3 - phase 1: request OTP ("Send Code")
    // ---------------------------------------------------------------------------------------------

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email).orElse(null);

        // Email Not Found: inform the caller so the UI can surface a clear message.
        if (user == null) {
            log.info("Password reset requested for unknown email: {}", maskEmail(email));
            return new ForgotPasswordResponse(GENERIC_SEND_MESSAGE, false, null, null);
        }

        // Alternative Course 4a - Account Locked: refuse to initiate the reset.
        if (isAccountLocked(user)) {
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Your account is locked. Please contact support to restore access.");
        }

        // Special Requirement #3 - Rate limiting: at most N codes within the window.
        enforceResendRateLimit(email);

        // Invalidate any earlier in-flight reset for this email so only the latest code is valid.
        invalidateActiveRequests(email);

        String otp = generateOtp();
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail(email);
        resetRequest.setOtpHash(passwordEncoder.encode(otp));
        resetRequest.setOtpExpiresAt(Instant.now().plusSeconds(codeExpirationSeconds));
        resetRequest.setAttemptCount(0);
        resetRequest.setStatus(PasswordResetStatus.PENDING_OTP);
        resetRequestRepository.save(resetRequest);

        // Exception 4b - SMTP Service Failure: sendResetCode throws SmtpServiceException (HTTP 503).
        String devCode = emailService.sendResetCode(email, otp);

        return new ForgotPasswordResponse(GENERIC_SEND_MESSAGE, true, codeExpirationSeconds, devCode);
    }

    // ---------------------------------------------------------------------------------------------
    // UC 1.3 - phase 2: verify OTP ("Verify & Continue")
    // ---------------------------------------------------------------------------------------------

    @Transactional
    public VerifyResetCodeResponse verifyCode(VerifyResetCodeRequest request) {
        String email = normalizeEmail(request.email());

        PasswordResetRequest resetRequest = resetRequestRepository
                .findByEmailAndStatus(email, PasswordResetStatus.PENDING_OTP)
                .stream()
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));

        // Alternative 7b - Expired Verification Code: 3-minute limit passed.
        if (resetRequest.getOtpExpiresAt() == null || resetRequest.getOtpExpiresAt().isBefore(Instant.now())) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
            resetRequestRepository.save(resetRequest);
            throw new ResponseStatusException(HttpStatus.GONE, "The code has expired");
        }

        // Too many invalid attempts on this code: invalidate and force a resend.
        if (resetRequest.getAttemptCount() >= maxVerifyAttempts) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
            resetRequestRepository.save(resetRequest);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many invalid attempts. Please request a new code.");
        }

        // Alternative 7a - Invalid Verification Code: increment attempts, stay on the same step.
        if (!passwordEncoder.matches(request.code(), resetRequest.getOtpHash())) {
            resetRequest.setAttemptCount(resetRequest.getAttemptCount() + 1);
            resetRequestRepository.save(resetRequest);
            throw new IllegalArgumentException("Invalid verification code");
        }

        // POST-1: identity verified. POST-2: issue a temporary reset token (UC 1.4 entry).
        Instant now = Instant.now();
        resetRequest.setStatus(PasswordResetStatus.VERIFIED);
        resetRequest.setResetToken(UUID.randomUUID().toString());
        resetRequest.setResetTokenExpiresAt(now.plusSeconds(resetTokenTimeoutSeconds));
        resetRequest.setLastActivityAt(now);
        // OTP can no longer be reused once verified.
        resetRequest.setOtpHash(null);
        resetRequestRepository.save(resetRequest);

        return new VerifyResetCodeResponse(resetRequest.getResetToken(), resetTokenTimeoutSeconds);
    }

    // ---------------------------------------------------------------------------------------------
    // UC 1.4 - set the new password ("Set Password")
    // ---------------------------------------------------------------------------------------------

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Alternative Course 4d - Session/Token Expiry: validate the security token first.
        PasswordResetRequest resetRequest = resetRequestRepository
                .findByResetToken(request.resetToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.GONE, "Session expired. Please restart the process."));

        if (!PasswordResetStatus.VERIFIED.equals(resetRequest.getStatus())
                || resetRequest.getResetTokenExpiresAt() == null
                || resetRequest.getResetTokenExpiresAt().isBefore(Instant.now())) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
            resetRequestRepository.save(resetRequest);
            throw new ResponseStatusException(
                    HttpStatus.GONE, "Session expired. Please restart the process.");
        }

        // Alternative Course 4a - Password Mismatch.
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Alternative Course 4b - Weak Password: specific complexity message.
        validatePasswordComplexity(request.newPassword());

        User user = userRepository.findByEmail(resetRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.GONE, "Session expired. Please restart the process."));

        // Alternative Course 4c - Password History Violation: cannot reuse the last 3 passwords.
        if (matchesRecentPassword(user, request.newPassword())) {
            throw new IllegalArgumentException("You cannot use your last 3 passwords.");
        }

        String newHash = passwordEncoder.encode(request.newPassword());

        try {
            // POST-1: update the credential. BCrypt work factor 10 (Spring default).
            user.setPasswordHash(newHash);
            // A successful reset also clears any lockout so the student can log in immediately.
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);

            // Record the new password for future history checks (Special Requirement #4).
            passwordHistoryRepository.save(new PasswordHistory(user.getId(), newHash));

            // POST-2: invalidate the reset token (single use).
            resetRequest.setStatus(PasswordResetStatus.USED);
            resetRequest.setResetToken(null);
            resetRequestRepository.save(resetRequest);
        } catch (DataAccessException ex) {
            // Exception 5a - Database Transaction Error.
            log.error("Database error while updating password for {}", maskEmail(user.getEmail()), ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "System Error: Password could not be updated. Please try again.");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------------------------------

    private boolean isAccountLocked(User user) {
        if (user.getLockTime() == null) {
            return false;
        }
        return user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MINUTES).isAfter(LocalDateTime.now());
    }

    private void enforceResendRateLimit(String email) {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(resendWindowMinutes));
        long recentCount = resetRequestRepository.countByEmailAndCreatedAtAfter(email, windowStart);
        if (recentCount >= resendMaxRequests) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many reset requests. Please try again later.");
        }
    }

    private void invalidateActiveRequests(String email) {
        List<PasswordResetRequest> pending =
                resetRequestRepository.findByEmailAndStatus(email, PasswordResetStatus.PENDING_OTP);
        for (PasswordResetRequest existing : pending) {
            existing.setStatus(PasswordResetStatus.CANCELED);
        }
        if (!pending.isEmpty()) {
            resetRequestRepository.saveAll(pending);
        }
    }

    private boolean matchesRecentPassword(User user, String rawPassword) {
        List<PasswordHistory> recent = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, historyCount));
        for (PasswordHistory history : recent) {
            if (passwordEncoder.matches(rawPassword, history.getPasswordHash())) {
                return true;
            }
        }
        // Defensively also compare against the currently stored hash.
        return user.getPasswordHash() != null && passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * UC 1.4 Special Requirement #1: at least 8 characters with one uppercase, one lowercase and
     * one digit. Each failure returns its own message (Alternative Course 4b).
     */
    private void validatePasswordComplexity(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    private String generateOtp() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + (at >= 0 ? email.substring(at) : "");
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
