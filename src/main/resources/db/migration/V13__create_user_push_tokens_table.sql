-- Migration V13: Create user_push_tokens table and index
CREATE TABLE user_push_tokens (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512)    NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_push_tokens_user_id ON user_push_tokens(user_id);
