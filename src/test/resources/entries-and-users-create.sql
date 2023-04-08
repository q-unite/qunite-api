INSERT INTO users VALUES (1, 'First', 'Creator');
INSERT INTO queues (id, name, creator_id, created_at) VALUES (1, 'First queue', 1, now());

INSERT INTO users VALUES (2, 'First', 'Member'),
                         (3, 'Second', 'Member'),
                         (4, 'Third', 'Member'),
                         (5, 'Fourth', 'Member'),
                         (6, 'Fifth', 'Member');

INSERT INTO entries (id, member_id, queue_id, created_at)
VALUES (27, 2, 1, now()),
       (22, 3, 1, now() + interval '1 minutes'),
       (19, 4, 1, now() + interval '2 minutes'),
       (14, 5, 1, now() + interval '3 minutes'),
       (1, 6, 1, now() + interval '4 minutes')
