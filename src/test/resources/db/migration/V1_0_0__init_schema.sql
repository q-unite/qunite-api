CREATE TABLE entries
(
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    entry_index INTEGER,
    member_id   BIGINT NOT NULL,
    queue_id    BIGINT NOT NULL,
    CONSTRAINT pk_entries PRIMARY KEY (member_id, queue_id)
);

CREATE TABLE queues
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name       VARCHAR(255),
    creator_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_queues PRIMARY KEY (id)
);

CREATE TABLE queues_managers
(
    manager_id BIGINT NOT NULL,
    queue_id   BIGINT NOT NULL,
    CONSTRAINT pk_queues_managers PRIMARY KEY (manager_id, queue_id)
);

CREATE TABLE users
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    username    VARCHAR(255),
    email    VARCHAR(255),
    password VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE entries
    ADD CONSTRAINT fk_entries_on_member FOREIGN KEY (member_id) REFERENCES users (id);

ALTER TABLE entries
    ADD CONSTRAINT fk_entries_on_queue FOREIGN KEY (queue_id) REFERENCES queues (id);

ALTER TABLE queues
    ADD CONSTRAINT fk_queues_on_creator FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE queues_managers
    ADD CONSTRAINT fk_queman_on_queue FOREIGN KEY (queue_id) REFERENCES queues (id);

ALTER TABLE queues_managers
    ADD CONSTRAINT fk_queman_on_user FOREIGN KEY (manager_id) REFERENCES users (id);