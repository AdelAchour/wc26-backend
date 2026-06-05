package com.adel.features.notifications.domain

import com.adel.features.users.domain.User
import java.time.OffsetDateTime

enum class NotificationType {
    LIKE_POST,
    REPLY_POST,
    LIKE_COMMENT
}

data class NotificationPostSnippet(
    val id: Long,
    val content: String
)

data class NotificationCommentSnippet(
    val id: Long,
    val content: String
)

data class Notification(
    val id: Long,
    val receiverId: Long,
    val sender: User,
    val type: NotificationType,
    val post: NotificationPostSnippet,
    val comment: NotificationCommentSnippet?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime
)