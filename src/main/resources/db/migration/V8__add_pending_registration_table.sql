CREATE TABLE pending_registrations (
    id BIGSERIAL PRIMARY KEY,
    verification_session_id VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    identity_document_type VARCHAR(20) NOT NULL,
    tc_identity_number VARCHAR(20),
    identity_serial_no VARCHAR(30),
    passport_number VARCHAR(30),
    passport_expiration_date DATE,
    current_program VARCHAR(255) NOT NULL,
    current_university VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    otp_expires_at TIMESTAMPTZ NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_pending_registrations_status
        CHECK (status IN ('PENDING', 'CANCELED', 'VERIFIED', 'EXPIRED')),
    CONSTRAINT chk_pending_registrations_identity_document_type
        CHECK (identity_document_type IN ('TC_ID', 'PASSPORT'))
);

CREATE INDEX idx_pending_registrations_email_status
    ON pending_registrations (email, status);

CREATE INDEX idx_pending_registrations_expires_at
    ON pending_registrations (otp_expires_at);
