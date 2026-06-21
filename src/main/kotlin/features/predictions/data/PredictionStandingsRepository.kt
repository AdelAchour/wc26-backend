package com.adel.features.predictions.data

import com.adel.features.predictions.domain.LeaderboardEntry
import com.adel.features.predictions.domain.MyRank
import com.adel.features.predictions.domain.RankedStanding

interface PredictionStandingsRepository {
    /** Recompute and upsert standings for each user from their graded predictions. */
    suspend fun recomputeForUsers(userIds: Collection<Long>)

    /** A page of the leaderboard, ranked. Rank is the absolute position (1-based). */
    suspend fun topEntries(limit: Int, offset: Long): List<LeaderboardEntry>

    /** Total number of ranked users (for pagination). */
    suspend fun count(): Long

    /** The given user's rank + standing, or null if they have no graded predictions yet. */
    suspend fun findMyRank(userId: Long): MyRank?

    /** Like [findMyRank] but with the full standing (incl. graded count) for profile stats. */
    suspend fun findRankedStanding(userId: Long): RankedStanding?
}
