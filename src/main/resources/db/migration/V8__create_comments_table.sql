-- Comments are user-created replies on posts.
-- Each comment belongs to one user (author) and one post.
--
-- ON DELETE CASCADE in both directions:
--   - If a user is deleted, their comments go too.
--   - If a post is deleted, its comments go too.
--
-- Also adds the denormalized comment_count column to posts.
-- Following the same pattern as like_count: the comments feature is
-- responsible for keeping it accurate via increments/decrements.

CREATE TABLE comments (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         BIGINT          NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    content         TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT comments_content_length_check
        CHECK (LENGTH(content) BETWEEN 1 AND 300)
);

-- Newest-first ordering on per-post lookup
CREATE INDEX idx_comments_post_created ON comments (post_id, created_at DESC);

-- For "comments by user" if we ever expose that endpoint
CREATE INDEX idx_comments_user_created ON comments (user_id, created_at DESC);

-- Add denormalized comment_count to posts
ALTER TABLE posts
    ADD COLUMN comment_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE posts
    ADD CONSTRAINT posts_comment_count_nonneg_check
        CHECK (comment_count >= 0);