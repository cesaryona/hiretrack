CREATE TABLE candidate (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

CREATE TABLE job_application (
    id            UUID PRIMARY KEY,
    job_id        UUID         NOT NULL,
    candidate_id  UUID         NOT NULL REFERENCES candidate (id),
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    UNIQUE (job_id, candidate_id)
);
