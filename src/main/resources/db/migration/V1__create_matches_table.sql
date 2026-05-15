CREATE TABLE matches (
    id              BIGSERIAL       PRIMARY KEY,
    home_team       VARCHAR(100)    NOT NULL,
    away_team       VARCHAR(100)    NOT NULL,
    stage           VARCHAR(50)     NOT NULL,
    venue           VARCHAR(150)    NOT NULL,
    country_code    VARCHAR(2)      NOT NULL,
    kickoff_at      TIMESTAMPTZ     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'scheduled',
    home_score      SMALLINT,
    away_score      SMALLINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT matches_status_check
        CHECK (status IN ('scheduled', 'live', 'finished')),
    CONSTRAINT matches_country_check
        CHECK (country_code IN ('US', 'CA', 'MX'))
);

CREATE INDEX idx_matches_status ON matches (status);
CREATE INDEX idx_matches_kickoff_at ON matches (kickoff_at);
CREATE INDEX idx_matches_stage ON matches (stage);
CREATE INDEX idx_matches_country_code ON matches (country_code);