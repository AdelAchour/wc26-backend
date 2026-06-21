package com.adel.features.predictions.data

import com.adel.features.matches.data.MatchTable
import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object PredictionTable : Table("predictions") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UserTable.id)
    val matchId = long("match_id").references(MatchTable.id)
    val homeScore = short("home_score")
    val awayScore = short("away_score")
    // Null until the match finishes and the scoring job grades the prediction.
    val pointsAwarded = short("points_awarded").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
