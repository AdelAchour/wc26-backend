package com.adel.features.notifications.data

import com.adel.features.users.data.UserTable
import com.adel.features.posts.data.PostTable
import com.adel.features.comments.data.CommentTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object NotificationTable : Table("notifications") {
    val id = long("id").autoIncrement()
    val receiverId = long("receiver_id").references(UserTable.id)
    val senderId = long("sender_id").references(UserTable.id)
    val type = varchar("type", 30)
    val postId = long("post_id").references(PostTable.id)
    val commentId = long("comment_id").references(CommentTable.id).nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}