INSERT INTO entries (id, member_id, queue_id, created_at)
VALUES (27, 7, 1, now()),
       (22, 6, 1, now() + interval '1 minutes'),
       (19, 5, 1, now() + interval '2 minutes'),
       (14, 4, 1, now() + interval '3 minutes'),
       (1, 3, 1, now() + interval '4 minutes');