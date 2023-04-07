INSERT INTO users VALUES (1, 'First', 'First');
INSERT INTO users VALUES (2, 'Second', 'Second');
INSERT INTO queues (id, name, creator_id, created_at) VALUES (1, 'First queue', 1, now());
INSERT INTO entries (id, member_id, queue_id, created_at) VALUES (1, 1, 1, now())
