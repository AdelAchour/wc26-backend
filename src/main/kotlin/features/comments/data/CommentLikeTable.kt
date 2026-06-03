package com.adel.features.comments.data

import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CommentLikeTable : Table("comment_likes") {
    val userId = long("user_id").references(UserTable.id)
    val commentId = long("comment_id").references(CommentTable.id)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(userId, commentId)
}