package com.adel.features.predictions.data

import com.adel.common.database.dbQuery
import com.adel.features.predictions.domain.Prediction
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class PredictionRepositoryImpl : PredictionRepository {

    override suspend fun findByUser(userId: Long): List<Prediction> = dbQuery {
        PredictionTable
            .selectAll()
            .where { PredictionTable.userId eq userId }
            .orderBy(PredictionTable.matchId to SortOrder.ASC)
            .map { it.toPrediction() }
    }

    override suspend fun findByUserAndMatch(userId: Long, matchId: Long): Prediction? = dbQuery {
        findByUserAndMatchInternal(userId, matchId)
    }

    override suspend fun findByMatch(matchId: Long): List<Prediction> = dbQuery {
        PredictionTable
            .selectAll()
            .where { PredictionTable.matchId eq matchId }
            .map { it.toPrediction() }
    }

    override suspend fun countByUser(userId: Long): Long = dbQuery {
        PredictionTable
            .selectAll()
            .where { PredictionTable.userId eq userId }
            .count()
    }

    override suspend fun updatePointsForScoreline(
        matchId: Long,
        homeScore: Short,
        awayScore: Short,
        points: Short,
    ): Int = dbQuery {
        PredictionTable.update({
            (PredictionTable.matchId eq matchId) and
                (PredictionTable.homeScore eq homeScore) and
                (PredictionTable.awayScore eq awayScore)
        }) {
            it[PredictionTable.pointsAwarded] = points
        }
    }

    override suspend fun upsert(
        userId: Long,
        matchId: Long,
        homeScore: Short,
        awayScore: Short,
    ): Prediction = dbQuery {
        val existing = findByUserAndMatchInternal(userId, matchId)
        if (existing != null) {
            PredictionTable.update({
                (PredictionTable.userId eq userId) and (PredictionTable.matchId eq matchId)
            }) {
                it[PredictionTable.homeScore] = homeScore
                it[PredictionTable.awayScore] = awayScore
                it[PredictionTable.updatedAt] = OffsetDateTime.now()
            }
        } else {
            PredictionTable.insert {
                it[PredictionTable.userId] = userId
                it[PredictionTable.matchId] = matchId
                it[PredictionTable.homeScore] = homeScore
                it[PredictionTable.awayScore] = awayScore
            }
        }
        findByUserAndMatchInternal(userId, matchId)!!
    }

    private fun findByUserAndMatchInternal(userId: Long, matchId: Long): Prediction? =
        PredictionTable
            .selectAll()
            .where { (PredictionTable.userId eq userId) and (PredictionTable.matchId eq matchId) }
            .map { it.toPrediction() }
            .singleOrNull()

    private fun ResultRow.toPrediction(): Prediction = Prediction(
        id = this[PredictionTable.id],
        userId = this[PredictionTable.userId],
        matchId = this[PredictionTable.matchId],
        homeScore = this[PredictionTable.homeScore],
        awayScore = this[PredictionTable.awayScore],
        pointsAwarded = this[PredictionTable.pointsAwarded],
        createdAt = this[PredictionTable.createdAt],
        updatedAt = this[PredictionTable.updatedAt],
    )
}
