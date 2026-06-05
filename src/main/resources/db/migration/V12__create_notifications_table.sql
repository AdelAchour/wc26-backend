-- Migration V12: Create notifications table and indices
CREATE TABLE notifications (
                               id              BIGSERIAL       PRIMARY KEY,
                               receiver_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               sender_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               type            VARCHAR(30)     NOT NULL, -- 'LIKE_POST', 'REPLY_POST', 'LIKE_COMMENT'
                               post_id         BIGINT          NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                               comment_id      BIGINT          REFERENCES comments(id) ON DELETE CASCADE, -- NULL for LIKE_POST
                               is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
                               created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index for ordering a user's notification list
CREATE INDEX idx_notifications_receiver_created ON notifications (receiver_id, created_at DESC);

-- Index for counting/filtering unread notifications quickly
CREATE INDEX idx_notifications_receiver_unread ON notifications (receiver_id, is_read) WHERE is_read = FALSE;