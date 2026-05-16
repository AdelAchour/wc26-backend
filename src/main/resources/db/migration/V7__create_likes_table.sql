-- Likes are a join table between users and posts.
-- Composite primary key (user_id, post_id) enforces "a user can like a post
-- only once" at the database level.
--
-- ON DELETE CASCADE in both directions:
--   - If a user is deleted, all their likes go too.
--   - If a post is deleted, all its likes go too.
--
-- Index on post_id supports "who liked this post" queries.
-- The PK already covers (user_id, post_id) so user-based lookups
-- ("posts liked by this user") use the PK index.
--
-- A separate index on created_at would help "recent likes" queries —
-- we'll add it only if such a query appears in V2.
CREATE TABLE likes (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);
-- For the "list users who liked this post" endpoint.
-- Ordered by created_at DESC for "most recent likers first" semantics.
CREATE INDEX idx_likes_post_created ON likes (post_id, created_at DESC);
-- For the "list posts this user liked" endpoint.
-- The PK already starts with user_id, but we want ORDER BY created_at DESC
-- to be index-supported.
CREATE INDEX idx_likes_user_created ON likes (user_id, created_at DESC);
