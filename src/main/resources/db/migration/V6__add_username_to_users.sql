-- Add username (public handle) to users table.
-- Username is 3-20 chars, lowercase letters/digits/underscore, must start
-- with a letter. Stored without the leading '@'.
-- CITEXT allows case-insensitive lookups (alice == Alice).

ALTER TABLE users
    ADD COLUMN username CITEXT;

-- Backfill existing users with usernames derived from display_name.
-- For our handful of test users this is fine; in a real migration with
-- many users we'd generate semi-random fallbacks to avoid collisions.
UPDATE users
SET username = LOWER(REGEXP_REPLACE(display_name, '[^a-zA-Z0-9_]', '', 'g'))
WHERE username IS NULL;

-- Enforce constraints now that all rows have values.
ALTER TABLE users
    ALTER COLUMN username SET NOT NULL,
    ADD CONSTRAINT users_username_unique UNIQUE (username),
    ADD CONSTRAINT users_username_format_check
        CHECK (username ~ '^[a-z][a-z0-9_]{2,19}$');

CREATE INDEX idx_users_username ON users (username);