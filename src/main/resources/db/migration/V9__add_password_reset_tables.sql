-- UC 1.3 (Verify Identity for Password Reset) & UC 1.4 (Reset Password)
-- Stores the OTP phase and the temporary reset-token phase of the password recovery flow.

CREATE TABLE password_reset_requests (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(255),
    otp_expires_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    reset_token VARCHAR(64) UNIQUE,
    reset_token_expires_at TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_password_reset_requests_status
        CHECK (status IN ('PENDING_OTP', 'VERIFIED', 'USED', 'EXPIRED', 'CANCELED'))
);

CREATE INDEX idx_password_reset_requests_email_status
    ON password_reset_requests (email, status);

CREATE INDEX idx_password_reset_requests_reset_token
    ON password_reset_requests (reset_token);

CREATE INDEX idx_password_reset_requests_created_at
    ON password_reset_requests (email, created_at);

-- UC 1.4 Special Requirement #4 (History Check): keep the last passwords per user so a
-- newly chosen password can be compared against the previous 3 hashes.
CREATE TABLE password_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_password_history_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_history_user_created
    ON password_history (user_id, created_at DESC);

-- Seed the existing credential as the most recent history entry for every current user,
-- so the "last 3 passwords" rule already has data to compare against on the first reset.
INSERT INTO password_history (user_id, password_hash, created_at, updated_at)
SELECT id, password_hash, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users;
