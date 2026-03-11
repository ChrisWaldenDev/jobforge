CREATE TABLE csv_files (
    id            UUID        NOT NULL PRIMARY KEY,
    original_name VARCHAR(255),
    content_type  VARCHAR(255),
    data          BYTEA       NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);
