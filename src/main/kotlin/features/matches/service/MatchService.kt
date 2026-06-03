package com.adel.features.matches.service

import com.adel.common.pagination.PaginatedResult
import com.adel.features.matches.data.MatchRepository
import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus

class MatchService(
    private val repository: MatchRepository,
) {
    suspend fun listMatches(
        status: MatchStatus?,
        stage: String?,
        limit: Int,
        offset: Long,
    ): PaginatedResult<Match> {
        val items = repository.findAll(status, stage, limit, offset)
        val total = repository.count(status, stage)
        return PaginatedResult(items, total, limit, offset)
    }

    suspend fun getMatch(id: Long): Match? = repository.findById(id)

    suspend fun getMatchesByIds(ids: Collection<Long>): Map<Long, Match> =
        repository.findByIds(ids)

    suspend fun updateMatchAdmin(
        id: Long,
        homeScore: Short?,
        awayScore: Short?,
        status: MatchStatus?,
    ): UpdateMatchResult {
        // Validation: if everything is null, nothing to update.
        if (homeScore == null && awayScore == null && status == null) {
            return UpdateMatchResult.NothingToUpdate
        }

        // Fetch current match to check its database status
        val currentMatch = repository.findById(id) ?: return UpdateMatchResult.NotFound
        val targetStatus = status ?: currentMatch.status

        // Standard validation: score rules apply unless target status is scheduled
        if (targetStatus != MatchStatus.SCHEDULED) {
            val scoreCount = listOf(homeScore, awayScore).count { it != null }
            if (scoreCount == 1) {
                return UpdateMatchResult.InvalidScores
            }
            if ((homeScore != null && homeScore < 0) || (awayScore != null && awayScore < 0)) {
                return UpdateMatchResult.InvalidScores
            }
        }

        // Apply state transition rule for moving from SCHEDULED to LIVE/FINISHED
        val (finalHomeScore, finalAwayScore) = if (currentMatch.status == MatchStatus.SCHEDULED &&
            (targetStatus == MatchStatus.LIVE || targetStatus == MatchStatus.FINISHED)
        ) {
            if (homeScore == null && awayScore == null) {
                // Client did not explicitly send scores; auto-initialize to 0-0.
                Pair(0.toShort(), 0.toShort())
            } else {
                Pair(homeScore, awayScore)
            }
        } else {
            Pair(homeScore, awayScore)
        }

        val updated = repository.updateScoreAndStatus(
            id = id,
            homeScore = finalHomeScore,
            awayScore = finalAwayScore,
            status = status,
        ) ?: return UpdateMatchResult.NotFound

        return UpdateMatchResult.Success(updated)
    }
}

sealed interface UpdateMatchResult {
    data class Success(val match: Match) : UpdateMatchResult
    data object NotFound : UpdateMatchResult
    data object InvalidScores : UpdateMatchResult
    data object NothingToUpdate : UpdateMatchResult
}