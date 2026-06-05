-- V12: Add ROLE_DEAN and ROLE_INTIBAK roles; seed dean user for UC 4.1

INSERT INTO roles (name, description, created_by, updated_by)
VALUES
    ('ROLE_DEAN',    'Faculty dean role',               'system', 'system'),
    ('ROLE_INTIBAK', 'Course exemption committee role', 'system', 'system')
ON CONFLICT (name) DO NOTHING;

-- Seed dean user (initial password same as admin; change via password reset)
INSERT INTO users (email, password_hash, first_name, last_name, is_active, created_by, updated_by)
VALUES ('dean@utms.local',
        '$2a$10$q8n4J4Z1iW8L9jaM1dpBquD0nM9Qq2gM7o8l8N9hC2OQMWD6SNo5K',
        'Fakülte', 'Dekanı', TRUE, 'system', 'system')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_DEAN'
WHERE u.email = 'dean@utms.local'
ON CONFLICT DO NOTHING;
