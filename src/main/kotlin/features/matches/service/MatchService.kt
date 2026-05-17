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

    suspend fun updateMatchAdmin(
        id: Long,
        homeScore: Short?,
        awayScore: Short?,
        status: MatchStatus?,
    ): UpdateMatchResult {
        // Validate score consistency: if any score is provided, both should be
        // (you can't have a half-known score in a real match).
        val scoreCount = listOf(homeScore, awayScore).count { it != null }
        if (scoreCount == 1) {
            return UpdateMatchResult.InvalidScores
        }

        // Validate scores are non-negative (DB has no constraint on this)
        if ((homeScore != null && homeScore < 0) || (awayScore != null && awayScore < 0)) {
            return UpdateMatchResult.InvalidScores
        }

        // If nothing to update, return BadRequest (PATCH with empty body is wrong)
        if (homeScore == null && awayScore == null && status == null) {
            return UpdateMatchResult.NothingToUpdate
        }

        val updated = repository.updateScoreAndStatus(id, homeScore, awayScore, status)
            ?: return UpdateMatchResult.NotFound

        return UpdateMatchResult.Success(updated)
    }
}

sealed interface UpdateMatchResult {
    data class Success(val match: Match) : UpdateMatchResult
    data object NotFound : UpdateMatchResult
    data object InvalidScores : UpdateMatchResult
    data object NothingToUpdate : UpdateMatchResult
}