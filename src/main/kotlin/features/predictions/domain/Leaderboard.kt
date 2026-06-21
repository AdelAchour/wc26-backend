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
