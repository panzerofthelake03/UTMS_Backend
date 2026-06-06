-- V14: Add PENDING_DEAN_APPROVAL to all status constraints (missed in V12)
--       and add language_score column to evaluations for YGK pre-fill.

-- applications table
ALTER TABLE applications DROP CONSTRAINT chk_applications_status;
ALTER TABLE applications ADD CONSTRAINT chk_applications_status CHECK (
    status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- from_status
ALTER TABLE application_status_history DROP CONSTRAINT chk_status_history_from_status;
ALTER TABLE application_status_history ADD CONSTRAINT chk_status_history_from_status CHECK (
    from_status IS NULL OR from_status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- to_status
ALTER TABLE application_status_history DROP CONSTRAINT chk_status_history_to_status;
ALTER TABLE application_status_history ADD CONSTRAINT chk_status_history_to_status CHECK (
    to_status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- Store YKS language score so YGK can read it as pre-filled (read-only)
ALTER TABLE evaluations ADD COLUMN IF NOT EXISTS language_score DECIMAL(6,2);
