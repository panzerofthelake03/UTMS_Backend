-- V5: Add WAITING_EXAM_RESULT as a valid workflow status.

ALTER TABLE applications
    DROP CONSTRAINT chk_applications_status;

ALTER TABLE applications
    ADD CONSTRAINT chk_applications_status CHECK (
        status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'WAITING_EXAM_RESULT',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );

ALTER TABLE application_status_history
    DROP CONSTRAINT chk_status_history_from_status;

ALTER TABLE application_status_history
    ADD CONSTRAINT chk_status_history_from_status CHECK (
        from_status IS NULL OR from_status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'WAITING_EXAM_RESULT',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );

ALTER TABLE application_status_history
    DROP CONSTRAINT chk_status_history_to_status;

ALTER TABLE application_status_history
    ADD CONSTRAINT chk_status_history_to_status CHECK (
        to_status IN (
            'DRAFT',
            'SUBMITTED',
            'UNDER_OIDB_REVIEW',
            'UNDER_YDYO_REVIEW',
            'WAITING_EXAM_RESULT',
            'UNDER_YGK_REVIEW',
            'ACCEPTED',
            'REJECTED'
        )
    );
