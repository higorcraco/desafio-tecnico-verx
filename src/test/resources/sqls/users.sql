INSERT INTO users (id, name, email, password, created_at, updated_at) VALUES
    (-1, 'Owner User',  'owner@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (-2, 'Other User',  'other@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (-3, 'Admin User',  'admin@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (-4, 'Empty User',  'empty@example.com', 'hashed-password', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) VALUES (-1, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (-2, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (-3, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (-4, 'USER');
