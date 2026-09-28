CREATE TABLE application_status_history (
    id             UUID        NOT NULL PRIMARY KEY,
    application_id UUID        NOT NULL,
    previous_status VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL
);
