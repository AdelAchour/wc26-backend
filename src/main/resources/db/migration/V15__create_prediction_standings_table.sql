-- Denormalized per-user prediction standings, the source for the leaderboard.
--
-- Recomputed for the affected users whenever a match is graded, so leaderboard
-- reads never have to SUM over the whole predictions table. A user appears here
-- only once they have at least one graded prediction.
--
-- total_points : sum of points_awarded across the user's graded predictions
-- exact_count  : how many were exact scorelines (5 pts) — tiebreaker + stats
-- graded_count : how many predictions have been graded — for hit-rate stats

CREATE TABLE prediction_standings (
    user_id       BIGINT       PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_points  INTEGER      NOT NULL DEFAULT 0,
    exact_count   INTEGER      NOT NULL DEFAULT 0,
    graded_count  INTEGER      NOT NULL DEFAULT 0,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Ranking order: points desc, then exact-score count, then earliest user (id asc).
CREATE INDEX idx_standings_rank
    ON prediction_standings (total_points DESC, exact_count DESC, user_id ASC);
