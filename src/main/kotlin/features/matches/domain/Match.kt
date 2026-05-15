package com.adel.features.matches.domain

import java.time.OffsetDateTime

data class Match(
    val id: Long,
    val homeTeam: String,
    val awayTeam: String,
    val stage: String,
    val venue: String,
    val countryCode: String,
    val kickoffAt: OffsetDateTime,
    val status: MatchStatus,
    val homeScore: Short?,
    val awayScore: Short?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
