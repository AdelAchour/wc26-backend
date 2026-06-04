package com.adel.features.matches.api

import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.TeamCodes
import kotlinx.serialization.Serializable

@Serializable
data class MatchDto(
    val id: Long,
    val gameNumber: Short,
    val homeTeam: String,
    val homeTeamCode: String?,
    val awayTeam: String,
    val awayTeamCode: String?,
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
    gameNumber = gameNumber,
    homeTeam = homeTeam,
    homeTeamCode = TeamCodes.fromTeamName(homeTeam),
    awayTeam = awayTeam,
    awayTeamCode = TeamCodes.fromTeamName(awayTeam),
    stage = stage,
    venue = venue,
    countryCode = countryCode,
    kickoffAt = kickoffAt.toString(),
    status = status.value,
    homeScore = homeScore,
    awayScore = awayScore,
)