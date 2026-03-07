INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Task A', 'TODO', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'owner@example.com';

INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Task B', 'TODO', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'owner@example.com';

INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Task C', 'IN_PROGRESS', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'owner@example.com';

INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Task D', 'DONE', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'owner@example.com';


INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Other Todo', 'TODO', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'other@example.com';

INSERT INTO tasks (title, status, owner_id, created_at, updated_at)
SELECT 'Other Done', 'DONE', id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM users WHERE email = 'other@example.com';
