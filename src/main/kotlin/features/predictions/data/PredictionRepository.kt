package com.adel.features.predictions.data

import com.adel.features.predictions.domain.Prediction

interface PredictionRepository {
    /** All of a user's predictions, for hydrating the match list / detail / predictions tab. */
    suspend fun findByUser(userId: Long): List<Prediction>

    suspend fun findByUserAndMatch(userId: Long, matchId: Long): Prediction?

    /**
     * Creates or updates the user's prediction for a match (one per user+match,
     * enforced by the unique constraint). On update, refreshes updated_at and
     * leaves points_awarded untouched.
     */
    suspend fun upsert(userId: Long, matchId: Long, homeScore: Short, awayScore: Short): Prediction
}
