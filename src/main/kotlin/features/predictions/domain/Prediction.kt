package com.adel.features.predictions.domain

import java.time.OffsetDateTime

/**
 * A user's score prediction for a single match.
 *
 * One prediction per (user, match) — see the unique constraint on the table.
 * [pointsAwarded] is null until the match finishes and the scoring job grades
 * it; afterwards it holds the points earned under the active scoring model.
 */
data class Prediction(
    val id: Long,
    val userId: Long,
    val matchId: Long,
    val homeScore: Short,
    val awayScore: Short,
    val pointsAwarded: Short?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
