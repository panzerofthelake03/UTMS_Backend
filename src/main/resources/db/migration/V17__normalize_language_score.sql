-- V17: Normalize evaluation.language_score from raw YKS scale (0-500) to formula scale (0-100).
--      The composite score formula uses (languageScore / 100) * 40, so scores > 100 must be divided by 5.

UPDATE evaluations
SET language_score = round((language_score / 5), 2)
WHERE language_score IS NOT NULL
  AND language_score > 100;
