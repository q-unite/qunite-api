INSERT INTO users
VALUES (1, 'Creator', 'Creator');
INSERT INTO users
VALUES (2, 'Manager', 'Manager');

INSERT INTO queues (id, name, creator_id, created_at)
VALUES (1, 'First queue', 1, now());
INSERT INTO QUEUES_MANAGERS (queue_id, manager_id)
VALUES (1, 2)