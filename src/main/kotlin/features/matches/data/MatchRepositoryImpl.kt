package com.adel.features.matches.data

import com.adel.common.database.dbQuery
import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

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
            .orderBy(MatchTable.kickoffAt)
            .limit(limit)
            .offset(offset)
            .map { it.toMatch() }
    }

    override suspend fun findById(id: Long): Match? = dbQuery {
        MatchTable
            .selectAll()
            .where { MatchTable.id eq id }
            .map { it.toMatch() }
            .singleOrNull()
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