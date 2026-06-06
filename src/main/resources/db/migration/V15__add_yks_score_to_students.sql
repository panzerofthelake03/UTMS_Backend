-- V15: Add YKS score to students; pre-populate with random demo values.
--       Back-fill evaluation.language_score from student.yks_score.

ALTER TABLE students ADD COLUMN IF NOT EXISTS yks_score DECIMAL(6,2);

-- Assign random YKS scores (250–500) to existing students without one
UPDATE students
SET yks_score = round((250 + random() * 250)::numeric, 2)
WHERE yks_score IS NULL;

-- Back-fill evaluation language_score from student yks_score
UPDATE evaluations ev
SET language_score = s.yks_score
FROM applications a
JOIN students s ON s.id = a.student_id
WHERE ev.application_id = a.id
  AND ev.language_score IS NULL
  AND s.yks_score IS NOT NULL;
