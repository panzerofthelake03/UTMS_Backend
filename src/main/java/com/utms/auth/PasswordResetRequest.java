package com.utms.auth;

import com.utms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Tracks a single password-recovery flow for an account (UC 1.3 + UC 1.4).
 *
 * <p>Phase 1 (UC 1.3): an OTP is generated, hashed into {@code otpHash} and stored with
 * {@code otpExpiresAt}. After the student enters the correct OTP the row transitions to
 * {@code VERIFIED} and a temporary {@code resetToken} is issued.</p>
 *
 * <p>Phase 2 (UC 1.4): the student exchanges the {@code resetToken} for a new password. The
 * token expires after a period of inactivity ({@code resetTokenExpiresAt}, refreshed via
 * {@code lastActivityAt}) and is single-use (status moves to {@code USED}).</p>
 */
@Entity
@Table(name = "password_reset_requests")
public class PasswordResetRequest extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "otp_hash", length = 255)
    private String otpHash;

    @Column(name = "otp_expires_at")
    private Instant otpExpiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "reset_token", unique = true, length = 64)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public Instant getOtpExpiresAt() {
        return otpExpiresAt;
    }

    public void setOtpExpiresAt(Instant otpExpiresAt) {
        this.otpExpiresAt = otpExpiresAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Instant getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(Instant resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
