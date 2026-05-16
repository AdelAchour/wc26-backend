package com.adel.features.posts.data

import com.adel.features.matches.data.MatchTable
import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object PostTable : Table("posts") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UserTable.id)
    val matchId = long("match_id").references(MatchTable.id)
    val content = text("content")
    val likeCount = integer("like_count")
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}