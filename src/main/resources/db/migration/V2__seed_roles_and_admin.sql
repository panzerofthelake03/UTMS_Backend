INSERT INTO roles (name, description, created_by, updated_by)
VALUES
    ('ROLE_STUDENT', 'Student role', 'system', 'system'),
    ('ROLE_OIDB', 'Administrative office role', 'system', 'system'),
    ('ROLE_YDYO', 'Foreign languages school role', 'system', 'system'),
    ('ROLE_YGK', 'Evaluation committee role', 'system', 'system'),
    ('ROLE_ADMIN', 'System administrator role', 'system', 'system');

INSERT INTO users (email, password_hash, first_name, last_name, is_active, created_by, updated_by)
VALUES ('admin@utms.local', '$2a$10$q8n4J4Z1iW8L9jaM1dpBquD0nM9Qq2gM7o8l8N9hC2OQMWD6SNo5K', 'System', 'Admin', TRUE, 'system', 'system');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@utms.local';
