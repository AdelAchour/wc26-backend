package com.adel.features.notifications.api

import com.adel.features.notifications.domain.Notification
import com.adel.features.notifications.domain.NotificationType
import com.adel.features.posts.api.PostAuthorDto
import kotlinx.serialization.Serializable

@Serializable
data class NotificationPostSnippetDto(
    val id: Long,
    val content: String
)

@Serializable
data class NotificationCommentSnippetDto(
    val id: Long,
    val content: String
)

@Serializable
data class NotificationDto(
    val id: Long,
    val receiverId: Long,
    val type: String, // "LIKE_POST", "REPLY_POST", "LIKE_COMMENT"
    val isRead: Boolean,
    val createdAt: String,
    val sender: PostAuthorDto,
    val post: NotificationPostSnippetDto,
    val comment: NotificationCommentSnippetDto?
)

@Serializable
data class UnreadCountDto(
    val unreadCount: Long
)

fun Notification.toDto(): NotificationDto = NotificationDto(
    id = id,
    receiverId = receiverId,
    type = type.name,
    isRead = isRead,
    createdAt = createdAt.toString(),
    sender = PostAuthorDto(
        id = sender.id,
        username = sender.username,
        displayName = sender.displayName,
        avatarUrl = sender.avatarUrl
    ),
    post = NotificationPostSnippetDto(
        id = post.id,
        content = post.content
    ),
    comment = comment?.let {
        NotificationCommentSnippetDto(
            id = it.id,
            content = it.content
        )
    }
)