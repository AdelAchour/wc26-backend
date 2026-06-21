package com.adel.features.predictions.api

import com.adel.features.predictions.domain.Prediction
import kotlinx.serialization.Serializable

/**
 * A user's prediction as sent to the client. Field names mirror [com.adel.features.matches.api.MatchDto]
 * (homeScore/awayScore) so the two line up on the client.
 *
 * [pointsAwarded] is null while the match is still open or live; it is populated
 * once the match finishes and the prediction is graded.
 */
@Serializable
data class PredictionDto(
    val matchId: Long,
    val homeScore: Short,
    val awayScore: Short,
    val pointsAwarded: Short?,
    val updatedAt: String,
)

/**
 * Request body for creating or updating a prediction (PUT /predictions/{matchId}).
 * The match id comes from the path, not the body.
 */
@Serializable
data class UpsertPredictionRequest(
    val homeScore: Short,
    val awayScore: Short,
)

fun Prediction.toDto(): PredictionDto = PredictionDto(
    matchId = matchId,
    homeScore = homeScore,
    awayScore = awayScore,
    pointsAwarded = pointsAwarded,
    updatedAt = updatedAt.toString(),
)
