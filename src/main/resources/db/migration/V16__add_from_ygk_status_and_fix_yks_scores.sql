-- V16: Add FROM_YGK application status (returned by YGK to OIDB when YKS score is missing).
--      Re-fill any students still missing a random demo YKS score and back-fill evaluations.

-- Add FROM_YGK to applications status constraint
ALTER TABLE applications DROP CONSTRAINT chk_applications_status;
ALTER TABLE applications ADD CONSTRAINT chk_applications_status CHECK (
    status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'FROM_YGK',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- Add FROM_YGK to status history from_status constraint
ALTER TABLE application_status_history DROP CONSTRAINT chk_status_history_from_status;
ALTER TABLE application_status_history ADD CONSTRAINT chk_status_history_from_status CHECK (
    from_status IS NULL OR from_status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'FROM_YGK',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- Add FROM_YGK to status history to_status constraint
ALTER TABLE application_status_history DROP CONSTRAINT chk_status_history_to_status;
ALTER TABLE application_status_history ADD CONSTRAINT chk_status_history_to_status CHECK (
    to_status IN (
        'DRAFT',
        'SUBMITTED',
        'UNDER_OIDB_REVIEW',
        'FROM_YGK',
        'UNDER_YDYO_REVIEW',
        'WAITING_EXAM_RESULT',
        'UNDER_YGK_REVIEW',
        'PENDING_DEAN_APPROVAL',
        'ACCEPTED',
        'REJECTED'
    )
);

-- Re-fill any students still missing a demo YKS score (created after V15 ran)
UPDATE students
SET yks_score = round((250 + random() * 250)::numeric, 2)
WHERE yks_score IS NULL;

-- Back-fill evaluation language_score from student yks_score (pick up rows missed by V15)
UPDATE evaluations ev
SET language_score = s.yks_score
FROM applications a
JOIN students s ON s.id = a.student_id
WHERE ev.application_id = a.id
  AND ev.language_score IS NULL
  AND s.yks_score IS NOT NULL;
