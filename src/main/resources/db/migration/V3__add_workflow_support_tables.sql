CREATE TABLE application_status_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    actor_user_id BIGINT,
    note TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_status_history_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_actor_user FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_status_history_from_status CHECK (
        from_status IS NULL OR from_status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED')
    ),
    CONSTRAINT chk_status_history_to_status CHECK (
        to_status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED')
    )
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    application_id BIGINT,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_notifications_recipient_user FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE SET NULL,
    CONSTRAINT chk_notifications_read_consistency CHECK (
        (is_read = FALSE AND read_at IS NULL) OR
        (is_read = TRUE AND read_at IS NOT NULL)
    )
);

CREATE INDEX idx_status_history_application_id ON application_status_history (application_id);
CREATE INDEX idx_status_history_changed_at ON application_status_history (changed_at);

CREATE INDEX idx_notifications_recipient_user_id ON notifications (recipient_user_id);
CREATE INDEX idx_notifications_recipient_read ON notifications (recipient_user_id, is_read);
CREATE INDEX idx_notifications_sent_at ON notifications (sent_at);