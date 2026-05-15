package com.adel.features.matches

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object MatchTable : Table("matches") {
    val id = long("id").autoIncrement()
    val homeTeam = varchar("home_team", 100)
    val awayTeam = varchar("away_team", 100)
    val stage = varchar("stage", 50)
    val venue = varchar("venue", 150)
    val countryCode = char("country_code", 2)
    val kickoffAt = timestampWithTimeZone("kickoff_at")
    val status = varchar("status", 20)
    val homeScore = short("home_score").nullable()
    val awayScore = short("away_score").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}