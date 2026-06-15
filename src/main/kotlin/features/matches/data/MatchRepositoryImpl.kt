package com.adel.features.matches.data

import com.adel.common.database.dbQuery
import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class MatchRepositoryImpl : MatchRepository {

    override suspend fun findAll(
        status: MatchStatus?,
        stage: String?,
        limit: Int,
        offset: Long,
    ): List<Match> = dbQuery {
        MatchTable
            .selectAll()
            .apply {
                status?.let { andWhere { MatchTable.status eq it.value } }
                stage?.let { andWhere { MatchTable.stage eq it } }
            }
            .orderBy(MatchTable.kickoffAt to if (status == MatchStatus.FINISHED) SortOrder.DESC else SortOrder.ASC)
            .limit(limit)
            .offset(offset)
            .map { it.toMatch() }
    }

    override suspend fun findById(id: Long): Match? = dbQuery {
        findByIdInternal(id)
    }

    override suspend fun findByIds(ids: Collection<Long>): Map<Long, Match> = dbQuery {
        if (ids.isEmpty()) return@dbQuery emptyMap()
        MatchTable
            .selectAll()
            .where { MatchTable.id inList ids }
            .map { it.toMatch() }
            .associateBy { it.id }
    }

    override suspend fun count(status: MatchStatus?, stage: String?): Long = dbQuery {
        MatchTable
            .selectAll()
            .apply {
                status?.let { andWhere { MatchTable.status eq it.value } }
                stage?.let { andWhere { MatchTable.stage eq it } }
            }
            .count()
    }

    override suspend fun updateScoreAndStatus(
        id: Long,
        homeScore: Short?,
        awayScore: Short?,
        status: MatchStatus?,
        homeTeam: String?,
        awayTeam: String?,
    ): Match? = dbQuery {
        // If nothing to update, return early.
        if (homeScore == null && awayScore == null && status == null && homeTeam == null && awayTeam == null) {
            return@dbQuery findByIdInternal(id)
        }

        val updated = MatchTable.update({ MatchTable.id eq id }) { stmt ->
            if (status == MatchStatus.SCHEDULED) {
                // If setting to scheduled, force both scores to null in the database.
                stmt[MatchTable.homeScore] = null
                stmt[MatchTable.awayScore] = null
            } else {
                // Standard PATCH: only update scores if they are explicitly sent.
                homeScore?.let { stmt[MatchTable.homeScore] = it }
                awayScore?.let { stmt[MatchTable.awayScore] = it }
            }
            status?.let { stmt[MatchTable.status] = it.value }
            homeTeam?.let { stmt[MatchTable.homeTeam] = it }
            awayTeam?.let { stmt[MatchTable.awayTeam] = it }
            stmt[MatchTable.updatedAt] = OffsetDateTime.now()
        }
        if (updated == 0) return@dbQuery null
        findByIdInternal(id)
    }

    private fun findByIdInternal(id: Long): Match? =
        MatchTable
            .selectAll()
            .where { MatchTable.id eq id }
            .map { it.toMatch() }
            .singleOrNull()

    private fun ResultRow.toMatch(): Match = Match(
        id = this[MatchTable.id],
        gameNumber = this[MatchTable.gameNumber],
        homeTeam = this[MatchTable.homeTeam],
        awayTeam = this[MatchTable.awayTeam],
        stage = this[MatchTable.stage],
        venue = this[MatchTable.venue],
        countryCode = this[MatchTable.countryCode],
        kickoffAt = this[MatchTable.kickoffAt],
        status = MatchStatus.fromString(this[MatchTable.status]),
        homeScore = this[MatchTable.homeScore],
        awayScore = this[MatchTable.awayScore],
        createdAt = this[MatchTable.createdAt],
        updatedAt = this[MatchTable.updatedAt],
    )
}