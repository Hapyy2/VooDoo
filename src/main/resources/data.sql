INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$M5CS3c0hHmKT6bnfxRIIoutGuir1XbK68VscmrByRCByQ/gB8AJ4W', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role)
VALUES ('demo_user', '$2a$10$M5CS3c0hHmKT6bnfxRIIoutGuir1XbK68VscmrByRCByQ/gB8AJ4W', 'ROLE_USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO categories (name, color, user_id)
VALUES ('Praca', '#0d6efd', (SELECT id FROM users WHERE username = 'demo_user'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, color, user_id)
VALUES ('Dom', '#198754', (SELECT id FROM users WHERE username = 'demo_user'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, color, user_id)
VALUES ('Inne', '#6c757d', (SELECT id FROM users WHERE username = 'demo_user'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO tasks (title, description, status, due_date, category_id, user_id, created_at)
VALUES
('Dokończyć projekt VooDoo', 'Muszę napisać testy i wdrożyć data.sql', 'IN_PROGRESS', '2025-12-31 23:59:00',
 (SELECT id FROM categories WHERE name = 'Praca'),
 (SELECT id FROM users WHERE username = 'demo_user'),
 NOW());

INSERT INTO tasks (title, description, status, due_date, category_id, user_id, created_at)
VALUES
('Kupić mleko', 'I przy okazji chleb', 'TODO', '2025-12-30 10:00:00',
 (SELECT id FROM categories WHERE name = 'Dom'),
 (SELECT id FROM users WHERE username = 'demo_user'),
 NOW());

INSERT INTO tasks (title, description, status, due_date, category_id, user_id, created_at)
VALUES
('Zrobić pranie', 'Czarne rzeczy', 'DONE', '2023-01-01 10:00:00',
 (SELECT id FROM categories WHERE name = 'Dom'),
 (SELECT id FROM users WHERE username = 'demo_user'),
 NOW());

INSERT INTO tags (name, user_id)
VALUES ('Pilne', (SELECT id FROM users WHERE username = 'demo_user'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO tags (name, user_id)
VALUES ('Java', (SELECT id FROM users WHERE username = 'demo_user'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO task_tags (task_id, tag_id)
VALUES
((SELECT id FROM tasks WHERE title = 'Dokończyć projekt VooDoo' LIMIT 1), (SELECT id FROM tags WHERE name = 'Pilne')),
((SELECT id FROM tasks WHERE title = 'Dokończyć projekt VooDoo' LIMIT 1), (SELECT id FROM tags WHERE name = 'Java'));