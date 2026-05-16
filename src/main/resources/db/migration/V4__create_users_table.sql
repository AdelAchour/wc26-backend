-- Users table for authentication and authorship of posts/comments.
--
-- email is stored lowercase via CITEXT (case-insensitive text) — so
-- "Alice@Example.com" and "alice@example.com" are treated as the same user.
-- This avoids the classic bug of duplicate users with different casings.
--
-- password_hash stores the full BCrypt output ($2b$10$<salt><hash>).
-- TEXT is used (not VARCHAR(n)) since BCrypt output is fixed at 60 chars
-- but using TEXT future-proofs against algorithm changes (e.g. Argon2).
--
-- role enables future authorization. 'user' is the default; 'admin' unlocks
-- privileged endpoints (e.g. PATCH /admin/matches/{id}).

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    email           CITEXT          NOT NULL UNIQUE,
    password_hash   TEXT            NOT NULL,
    display_name    VARCHAR(50)     NOT NULL,
    avatar_url      TEXT,
    role            VARCHAR(10)     NOT NULL DEFAULT 'user',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT users_role_check
        CHECK (role IN ('user', 'admin')),
    CONSTRAINT users_email_format_check
        CHECK (email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    CONSTRAINT users_display_name_length_check
        CHECK (LENGTH(display_name) >= 2)
);

CREATE INDEX idx_users_role ON users (role);