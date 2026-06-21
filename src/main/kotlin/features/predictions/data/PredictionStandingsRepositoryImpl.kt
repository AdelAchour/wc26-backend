package com.adel.features.predictions.data

import com.adel.common.database.dbQuery
import com.adel.features.predictions.domain.LeaderboardEntry
import com.adel.features.predictions.domain.MyRank
import com.adel.features.predictions.domain.PredictionScoring
import com.adel.features.predictions.domain.RankedStanding
import com.adel.features.users.data.UserTable
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class PredictionStandingsRepositoryImpl : PredictionStandingsRepository {

    override suspend fun recomputeForUsers(userIds: Collection<Long>): Unit = dbQuery {
        userIds.distinct().forEach { userId ->
            val points = PredictionTable
                .selectAll()
                .where { (PredictionTable.userId eq userId) and PredictionTable.pointsAwarded.isNotNull() }
                .mapNotNull { it[PredictionTable.pointsAwarded]?.toInt() }

            val total = points.sum()
            val exact = points.count { it == PredictionScoring.EXACT_SCORE_POINTS.toInt() }
            val graded = points.size

            val exists = PredictionStandingsTable
                .selectAll()
                .where { PredictionStandingsTable.userId eq userId }
                .count() > 0

            if (exists) {
                PredictionStandingsTable.update({ PredictionStandingsTable.userId eq userId }) {
                    it[totalPoints] = total
                    it[exactCount] = exact
                    it[gradedCount] = graded
                    it[updatedAt] = OffsetDateTime.now()
                }
            } else {
                PredictionStandingsTable.insert {
                    it[PredictionStandingsTable.userId] = userId
                    it[totalPoints] = total
                    it[exactCount] = exact
                    it[gradedCount] = graded
                }
            }
        }
    }

    override suspend fun topEntries(limit: Int, offset: Long): List<LeaderboardEntry> = dbQuery {
        (PredictionStandingsTable innerJoin UserTable)
            .selectAll()
            .orderBy(
                PredictionStandingsTable.totalPoints to SortOrder.DESC,
                PredictionStandingsTable.exactCount to SortOrder.DESC,
                PredictionStandingsTable.userId to SortOrder.ASC,
            )
            .limit(limit)
            .offset(offset)
            .mapIndexed { index, row -> row.toLeaderboardEntry(rank = offset + index + 1) }
    }

    override suspend fun count(): Long = dbQuery {
        PredictionStandingsTable.selectAll().count()
    }

    override suspend fun findMyRank(userId: Long): MyRank? =
        findRankedStanding(userId)?.let { MyRank(it.rank, it.totalPoints, it.exactCount) }

    override suspend fun findRankedStanding(userId: Long): RankedStanding? = dbQuery {
        val me = PredictionStandingsTable
            .selectAll()
            .where { PredictionStandingsTable.userId eq userId }
            .singleOrNull()
            ?: return@dbQuery null

        val total = me[PredictionStandingsTable.totalPoints]
        val exact = me[PredictionStandingsTable.exactCount]
        val graded = me[PredictionStandingsTable.gradedCount]

        // Count everyone strictly ahead per the ranking order.
        val ahead = PredictionStandingsTable
            .selectAll()
            .where {
                (PredictionStandingsTable.totalPoints greater total) or
                    ((PredictionStandingsTable.totalPoints eq total) and (PredictionStandingsTable.exactCount greater exact)) or
                    ((PredictionStandingsTable.totalPoints eq total) and (PredictionStandingsTable.exactCount eq exact) and (PredictionStandingsTable.userId less userId))
            }
            .count()

        RankedStanding(rank = ahead + 1, totalPoints = total, exactCount = exact, gradedCount = graded)
    }

    private fun ResultRow.toLeaderboardEntry(rank: Long): LeaderboardEntry = LeaderboardEntry(
        rank = rank,
        user = User(
            id = this[UserTable.id],
            email = this[UserTable.email],
            username = this[UserTable.username],
            passwordHash = this[UserTable.passwordHash],
            displayName = this[UserTable.displayName],
            avatarUrl = this[UserTable.avatarUrl],
            bio = this[UserTable.bio],
            role = UserRole.fromString(this[UserTable.role]),
            createdAt = this[UserTable.createdAt],
            updatedAt = this[UserTable.updatedAt],
        ),
        totalPoints = this[PredictionStandingsTable.totalPoints],
        exactCount = this[PredictionStandingsTable.exactCount],
    )
}
