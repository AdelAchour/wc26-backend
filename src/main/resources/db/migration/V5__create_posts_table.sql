-- Posts are user-created takes attached to a specific match.
-- Every post belongs to one user (author) and one match (subject).
--
-- like_count is denormalized for fast feed reads (avoiding a COUNT() per post).
-- The likes feature is responsible for keeping it accurate via INC/DEC on
-- like/unlike. This is a classic eventual-consistency tradeoff: faster reads,
-- write complexity, occasional drift. For V1, we accept it.
--
-- ON DELETE CASCADE for user_id: if a user is deleted, their posts go too.
-- ON DELETE CASCADE for match_id: WC26 matches are immutable so this is
-- defensive — we'd never actually delete a match, but FK behavior should
-- still be specified.

CREATE TABLE posts (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_id        BIGINT          NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    content         TEXT            NOT NULL,
    like_count      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT posts_content_length_check
        CHECK (LENGTH(content) BETWEEN 1 AND 500),
    CONSTRAINT posts_like_count_nonneg_check
        CHECK (like_count >= 0)
);

-- Posts feed by match: order by created_at DESC.
-- Composite index supports the most common query pattern.
CREATE INDEX idx_posts_match_created ON posts (match_id, created_at DESC);

-- Posts by user (for profile screen)
CREATE INDEX idx_posts_user_created ON posts (user_id, created_at DESC);