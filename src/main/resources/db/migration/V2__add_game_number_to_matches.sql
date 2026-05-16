-- V2__add_game_number_to_matches.sql

ALTER TABLE matches
    ADD COLUMN game_number SMALLINT NOT NULL,
    ADD CONSTRAINT matches_game_number_unique UNIQUE (game_number),
    ADD CONSTRAINT matches_game_number_check CHECK (game_number BETWEEN 1 AND 104);

CREATE INDEX idx_matches_game_number ON matches (game_number);