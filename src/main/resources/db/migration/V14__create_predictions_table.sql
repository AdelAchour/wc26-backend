-- Predictions: a user's score prediction for a single match.
--
-- One prediction per (user, match), enforced by a unique constraint — writes
-- are upserts. ON DELETE CASCADE in both directions: a deleted user's
-- predictions go too, and a deleted match's predictions go too.
--
-- points_awarded is NULL until the match finishes and the scoring job grades
-- the prediction; afterwards it holds the points earned (2-tier model: 5 exact,
-- 3 correct result, 0 wrong).

CREATE TABLE predictions (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_id        BIGINT          NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    home_score      SMALLINT        NOT NULL,
    away_score      SMALLINT        NOT NULL,
    points_awarded  SMALLINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT predictions_user_match_unique UNIQUE (user_id, match_id),

    CONSTRAINT predictions_scores_range_check
        CHECK (home_score BETWEEN 0 AND 99 AND away_score BETWEEN 0 AND 99)
);

-- Scoring job: fetch every prediction for a match that just finished.
CREATE INDEX idx_predictions_match ON predictions (match_id);

-- "My predictions" lookups for the list / detail / stats screens.
CREATE INDEX idx_predictions_user ON predictions (user_id);
