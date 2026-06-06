-- UC 3.1.5 SRS: Department Specific Conditions — binary validation field
ALTER TABLE evaluations
    ADD COLUMN IF NOT EXISTS dept_conditions_verified BOOLEAN NOT NULL DEFAULT FALSE;
