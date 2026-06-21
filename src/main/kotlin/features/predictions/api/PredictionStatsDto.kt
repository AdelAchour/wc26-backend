package com.adel.features.predictions.api

import com.adel.features.predictions.domain.PredictionStats
import kotlinx.serialization.Serializable

@Serializable
data class PredictionStatsDto(
    val userId: Long,
    val rank: Long?,
    val totalPoints: Int,
    val exactCount: Int,
    val gradedCount: Int,
    val predictionsCount: Int,
)

fun PredictionStats.toDto(): PredictionStatsDto = PredictionStatsDto(
    userId = userId,
    rank = rank,
    totalPoints = totalPoints,
    exactCount = exactCount,
    gradedCount = gradedCount,
    predictionsCount = predictionsCount,
)
