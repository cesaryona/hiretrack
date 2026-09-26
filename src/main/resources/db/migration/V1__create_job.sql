CREATE TABLE job (
    id          UUID PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    company     VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
