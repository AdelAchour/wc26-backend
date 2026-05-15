package com.adel.features.matches.api

import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus
import com.adel.features.matches.service.PaginatedResult
import kotlinx.serialization.Serializable

@Serializable
data class MatchDto(
    val id: Long,
    val homeTeam: String,
    val awayTeam: String,
    val stage: String,
    val venue: String,
    val countryCode: String,
    val kickoffAt: String,
    val status: String,
    val homeScore: Short?,
    val awayScore: Short?,
)

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Long,
)

fun Match.toDto(): MatchDto = MatchDto(
    id = id,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    stage = stage,
    venue = venue,
    countryCode = countryCode,
    kickoffAt = kickoffAt.toString(),
    status = status.value,
    homeScore = homeScore,
    awayScore = awayScore,
)

fun PaginatedResult<Match>.toDto(): PageDto<MatchDto> = PageDto(
    items = items.map { it.toDto() },
    total = total,
    limit = limit,
    offset = offset,
)