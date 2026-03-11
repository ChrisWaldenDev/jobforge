CREATE TABLE jobs (
    id            UUID        NOT NULL PRIMARY KEY,
    type          VARCHAR(50) NOT NULL,
    status        VARCHAR(50) NOT NULL,
    payload       TEXT,
    result        TEXT,
    error         TEXT,
    attempts      INT         NOT NULL DEFAULT 0,
    max_attempts  INT         NOT NULL DEFAULT 3,
    next_run_at     TIMESTAMPTZ,
    scheduled_for   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ,
    locked_at     TIMESTAMPTZ,
    locked_by     VARCHAR(255)
);
