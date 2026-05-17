package com.adel.features.matches.data

import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus

interface MatchRepository {
    suspend fun findAll(
        status: MatchStatus? = null,
        stage: String? = null,
        limit: Int = 20,
        offset: Long = 0,
    ): List<Match>

    suspend fun findById(id: Long): Match?

    suspend fun count(status: MatchStatus? = null, stage: String? = null): Long

    suspend fun updateScoreAndStatus(
        id: Long,
        homeScore: Short?,
        awayScore: Short?,
        status: MatchStatus?,
    ): Match?
}