package com.adel.features.likes.data

import com.adel.features.posts.data.PostTable
import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LikeTable : Table("likes") {
    val userId = long("user_id").references(UserTable.id)
    val postId = long("post_id").references(PostTable.id)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(userId, postId)
}