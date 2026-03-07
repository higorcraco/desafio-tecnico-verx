INSERT INTO users (name, email, password, created_at, updated_at) VALUES
    ('Owner User',  'owner@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Other User',  'other@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Admin User',  'admin@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Empty User',  'empty@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) SELECT id, 'USER'  FROM users WHERE email = 'owner@example.com';
INSERT INTO user_roles (user_id, role) SELECT id, 'USER'  FROM users WHERE email = 'other@example.com';
INSERT INTO user_roles (user_id, role) SELECT id, 'ADMIN' FROM users WHERE email = 'admin@example.com';
INSERT INTO user_roles (user_id, role) SELECT id, 'USER'  FROM users WHERE email = 'empty@example.com';
