package com.adel.features.predictions.api

import com.adel.common.pagination.PageDto
import com.adel.features.predictions.domain.LeaderboardEntry
import com.adel.features.predictions.domain.MyRank
import com.adel.features.users.api.UserPublicDto
import com.adel.features.users.api.toPublicDto
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDto(
    val rank: Long,
    val user: UserPublicDto,
    val totalPoints: Int,
    val exactCount: Int,
)

@Serializable
data class MyRankDto(
    val rank: Long,
    val totalPoints: Int,
    val exactCount: Int,
)

/** Leaderboard page plus the viewer's own rank (null if not logged in or unranked). */
@Serializable
data class LeaderboardResponseDto(
    val entries: PageDto<LeaderboardEntryDto>,
    val me: MyRankDto?,
)

fun LeaderboardEntry.toDto(): LeaderboardEntryDto = LeaderboardEntryDto(
    rank = rank,
    user = user.toPublicDto(),
    totalPoints = totalPoints,
    exactCount = exactCount,
)

fun MyRank.toDto(): MyRankDto = MyRankDto(
    rank = rank,
    totalPoints = totalPoints,
    exactCount = exactCount,
)
