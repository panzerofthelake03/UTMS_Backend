ALTER TABLE students
    ADD COLUMN nationality VARCHAR(100),
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN identity_document_type VARCHAR(20),
    ADD COLUMN tc_identity_number VARCHAR(20),
    ADD COLUMN identity_serial_no VARCHAR(30),
    ADD COLUMN passport_number VARCHAR(30),
    ADD COLUMN passport_expiration_date DATE,
    ADD COLUMN current_program VARCHAR(255),
    ADD COLUMN current_university VARCHAR(255);

ALTER TABLE students
    ADD CONSTRAINT chk_students_identity_document_type
    CHECK (identity_document_type IS NULL OR identity_document_type IN ('TC_ID', 'PASSPORT'));

ALTER TABLE students
    ADD CONSTRAINT chk_students_identity_fields
    CHECK (
        identity_document_type IS NULL
        OR (
            identity_document_type = 'TC_ID'
            AND tc_identity_number IS NOT NULL
            AND identity_serial_no IS NOT NULL
            AND passport_number IS NULL
            AND passport_expiration_date IS NULL
        )
        OR (
            identity_document_type = 'PASSPORT'
            AND passport_number IS NOT NULL
            AND passport_expiration_date IS NOT NULL
            AND tc_identity_number IS NULL
            AND identity_serial_no IS NULL
        )
    );

CREATE UNIQUE INDEX uq_students_tc_identity_number
    ON students (tc_identity_number)
    WHERE tc_identity_number IS NOT NULL;

CREATE UNIQUE INDEX uq_students_passport_number
    ON students (passport_number)
    WHERE passport_number IS NOT NULL;
