-- Stores short-lived password-reset codes.
--
-- Each row represents a pending reset request. On successful reset
-- the row is deleted so the code cannot be reused. Expired rows are
-- harmless but can be cleaned up periodically.
--
-- email references the users table via CITEXT for case-insensitive
-- matching, consistent with how emails are stored in users.

CREATE TABLE password_resets (
    id          BIGSERIAL       PRIMARY KEY,
    email       CITEXT          NOT NULL,
    code        VARCHAR(6)      NOT NULL,
    expires_at  TIMESTAMPTZ     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_resets_email ON password_resets (email);
