package com.adel.features.comments.data

import com.adel.features.posts.data.PostTable
import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CommentTable : Table("comments") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UserTable.id)
    val postId = long("post_id").references(PostTable.id)
    val content = text("content")
    val likeCount = integer("like_count").default(0)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}