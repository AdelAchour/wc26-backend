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
}