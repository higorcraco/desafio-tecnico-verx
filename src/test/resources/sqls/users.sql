INSERT INTO users (id, name, email, password, created_at, updated_at) VALUES
    ('00000000-0000-7000-8000-000000000001', 'Owner User',  'owner@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-7000-8000-000000000002', 'Other User',  'other@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-7000-8000-000000000003', 'Admin User',  'admin@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-7000-8000-000000000004', 'Empty User',  'empty@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) VALUES ('00000000-0000-7000-8000-000000000001', 'USER');
INSERT INTO user_roles (user_id, role) VALUES ('00000000-0000-7000-8000-000000000002', 'USER');
INSERT INTO user_roles (user_id, role) VALUES ('00000000-0000-7000-8000-000000000003', 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES ('00000000-0000-7000-8000-000000000004', 'USER');
