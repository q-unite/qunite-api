INSERT INTO users VALUES (1, 'Creator', 'Creator'),
                         (2, 'Manager', 'Manager'),
                         (3, 'First', 'Member'),
                         (4, 'Second', 'Member'),
                         (5, 'Third', 'Member');

INSERT INTO queues (id, name, creator_id, created_at) VALUES (2, 'First queue', 1, now());

INSERT INTO entries (id, member_id, queue_id, created_at)
VALUES (27, 5, 2, now()),
       (22, 4, 2, now() + interval '1 minutes'),
       (19, 3, 2, now() + interval '2 minutes'),
       (14, 2, 2, now() + interval '3 minutes'),
       (1, 1, 2, now() + interval '4 minutes')
