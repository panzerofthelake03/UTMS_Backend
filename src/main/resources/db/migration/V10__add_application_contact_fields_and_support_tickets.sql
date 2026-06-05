-- UC 1.6: add targetDepartment, phone, address, englishProficiencyOption to applications
ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS target_department   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone               VARCHAR(30),
    ADD COLUMN IF NOT EXISTS address             TEXT,
    ADD COLUMN IF NOT EXISTS english_proficiency_option VARCHAR(30) DEFAULT 'DOCUMENT';

-- UC 1.7: support tickets table
CREATE TABLE IF NOT EXISTS support_tickets (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT        NOT NULL REFERENCES students(id),
    subject      VARCHAR(100)  NOT NULL,
    category     VARCHAR(50)   NOT NULL,
    message      TEXT          NOT NULL,
    ticket_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
