INSERT INTO entries (member_id, queue_id, created_at, entry_index)
VALUES (7, 1, now(), 0),
       (6, 1, now() + interval '1 minutes', 1),
       (5, 1, now() + interval '2 minutes', 2),
       (4, 1, now() + interval '3 minutes', 3),
       (3, 1, now() + interval '4 minutes', 4);