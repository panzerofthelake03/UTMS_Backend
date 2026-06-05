-- V11: Extend ydyo_decision constraint to allow inline list decisions (PASS, FAIL, DOCUMENT_REQUIRED)
-- The original V4 constraint only permitted APPROVED and EXAM_REQUIRED (document review values).
-- setInlineDecision also stores PASS / FAIL / DOCUMENT_REQUIRED in the same column.

ALTER TABLE evaluations DROP CONSTRAINT IF EXISTS chk_evaluations_ydyo_decision;

ALTER TABLE evaluations
    ADD CONSTRAINT chk_evaluations_ydyo_decision CHECK (
        ydyo_decision IS NULL
        OR ydyo_decision IN ('APPROVED', 'EXAM_REQUIRED', 'PASS', 'FAIL', 'DOCUMENT_REQUIRED')
    );
