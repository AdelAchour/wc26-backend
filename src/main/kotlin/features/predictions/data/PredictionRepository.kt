package com.adel.features.predictions.data

import com.adel.features.predictions.domain.Prediction

interface PredictionRepository {
    /** All of a user's predictions, for hydrating the match list / detail / predictions tab. */
    suspend fun findByUser(userId: Long): List<Prediction>

    suspend fun findByUserAndMatch(userId: Long, matchId: Long): Prediction?

    /** All predictions for a match — used by the scoring job when it finishes. */
    suspend fun findByMatch(matchId: Long): List<Prediction>

    /** Total number of predictions a user has made (including ungraded). */
    suspend fun countByUser(userId: Long): Long

    /**
     * Sets points_awarded for every prediction of [matchId] that picked the
     * given scoreline. Returns the number of rows updated. Idempotent — safe to
     * re-run if a match result is later corrected.
     */
    suspend fun updatePointsForScoreline(
        matchId: Long,
        homeScore: Short,
        awayScore: Short,
        points: Short,
    ): Int

    /**
     * Creates or updates the user's prediction for a match (one per user+match,
     * enforced by the unique constraint). On update, refreshes updated_at and
     * leaves points_awarded untouched.
     */
    suspend fun upsert(userId: Long, matchId: Long, homeScore: Short, awayScore: Short): Prediction
}
