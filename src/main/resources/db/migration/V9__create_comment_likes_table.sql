-- Add denormalized like_count to comments
ALTER TABLE comments
    ADD COLUMN like_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE comments
    ADD CONSTRAINT comments_like_count_nonneg_check
        CHECK (like_count >= 0);

-- Create comment_likes join table
CREATE TABLE comment_likes (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, comment_id)
);

-- Index for counting/listing who liked a comment
CREATE INDEX idx_comment_likes_comment_created ON comment_likes (comment_id, created_at DESC);

-- Index for listing comments liked by a user
CREATE INDEX idx_comment_likes_user_created ON comment_likes (user_id, created_at DESC);