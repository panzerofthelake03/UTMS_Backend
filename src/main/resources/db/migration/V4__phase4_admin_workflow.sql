-- V4: Expand application status values for Phase 4 admin workflow
--     and add the course_exemptions (intibak) table.

-- 1. Update applications status constraint
ALTER TABLE applications
    DROP CONSTRAINT chk_applications_status;

ALTER TABLE applications
    ADD CONSTRAINT chk_applications_status CHECK (
        status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );

-- 2. Update application_status_history from_status constraint
ALTER TABLE application_status_history
    DROP CONSTRAINT chk_status_history_from_status;

ALTER TABLE application_status_history
    ADD CONSTRAINT chk_status_history_from_status CHECK (
        from_status IS NULL OR from_status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );

-- 3. Update application_status_history to_status constraint
ALTER TABLE application_status_history
    DROP CONSTRAINT chk_status_history_to_status;

ALTER TABLE application_status_history
    ADD CONSTRAINT chk_status_history_to_status CHECK (
        to_status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );

-- 4. Extend evaluations decision to include ACCEPTED
ALTER TABLE evaluations
    DROP CONSTRAINT chk_evaluations_decision;

ALTER TABLE evaluations
    ADD CONSTRAINT chk_evaluations_decision CHECK (
        decision IS NULL OR decision IN (
            'PENDING',
            'APPROVED',
            'EXAM_REQUIRED',
            'ACCEPTED',
            'REJECTED'
        )
    );

-- 5. Add ydyo_review_note and ydyo_decision columns to evaluations
ALTER TABLE evaluations
    ADD COLUMN IF NOT EXISTS ydyo_decision  VARCHAR(20),
    ADD COLUMN IF NOT EXISTS ydyo_note      TEXT,
    ADD COLUMN IF NOT EXISTS ydyo_reviewer_id BIGINT,
    ADD CONSTRAINT chk_evaluations_ydyo_decision CHECK (
        ydyo_decision IS NULL OR ydyo_decision IN ('APPROVED', 'EXAM_REQUIRED')
    ),
    ADD CONSTRAINT fk_evaluations_ydyo_reviewer
        FOREIGN KEY (ydyo_reviewer_id) REFERENCES users (id) ON DELETE SET NULL;

-- 6. Course exemptions (Intibak) table
CREATE TABLE course_exemptions (
    id                      BIGSERIAL PRIMARY KEY,
    application_id          BIGINT        NOT NULL,
    student_course_code     VARCHAR(50)   NOT NULL,
    student_course_name     VARCHAR(200)  NOT NULL,
    student_course_credits  INTEGER       NOT NULL,
    student_course_grade    VARCHAR(10),
    target_course_code      VARCHAR(50),
    target_course_name      VARCHAR(200),
    target_course_credits   INTEGER,
    decision                VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    decision_note           TEXT,
    decided_by_user_id      BIGINT,
    decided_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    CONSTRAINT fk_exemptions_application  FOREIGN KEY (application_id)    REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_exemptions_decided_by   FOREIGN KEY (decided_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_exemptions_decision    CHECK (decision IN ('PENDING', 'EXEMPT', 'PARTIAL', 'REJECTED'))
);

CREATE INDEX idx_exemptions_application_id ON course_exemptions (application_id);
