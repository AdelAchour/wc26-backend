package com.adel.features.predictions.data

import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object PredictionStandingsTable : Table("prediction_standings") {
    val userId = long("user_id").references(UserTable.id)
    val totalPoints = integer("total_points")
    val exactCount = integer("exact_count")
    val gradedCount = integer("graded_count")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
