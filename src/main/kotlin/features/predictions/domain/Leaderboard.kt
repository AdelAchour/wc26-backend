package com.adel.features.predictions.domain

import com.adel.features.users.domain.User

/** One ranked row of the leaderboard — a user's standing plus their position. */
data class LeaderboardEntry(
    val rank: Long,
    val user: User,
    val totalPoints: Int,
    val exactCount: Int,
)

/** The requesting user's own position, for the sticky "you" row. */
data class MyRank(
    val rank: Long,
    val totalPoints: Int,
    val exactCount: Int,
)

/** A user's standing row plus their rank — the basis for profile stats. */
data class RankedStanding(
    val rank: Long,
    val totalPoints: Int,
    val exactCount: Int,
    val gradedCount: Int,
)

/** Full prediction stats for a user's profile. */
data class PredictionStats(
    val userId: Long,
    val rank: Long?,            // null until the user has a graded prediction
    val totalPoints: Int,
    val exactCount: Int,
    val gradedCount: Int,
    val predictionsCount: Int, // total predictions made, including ungraded
)
