ALTER TABLE tasks
    ADD COLUMN owner_id BIGINT;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id)
            REFERENCES app_users(id);

CREATE INDEX idx_tasks_owner_id
    ON tasks(owner_id);