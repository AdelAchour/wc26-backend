package com.adel.features.matches.api

import com.adel.features.matches.domain.Match
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