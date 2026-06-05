package com.adel.features.notifications.data

import com.adel.common.pagination.PaginatedResult
import com.adel.features.notifications.domain.Notification
import com.adel.features.notifications.domain.NotificationType

interface NotificationRepository {
    suspend fun create(
        senderId: Long,
        receiverId: Long,
        type: NotificationType,
        postId: Long,
        commentId: Long?
    ): Boolean

    suspend fun findNotificationsForUser(
        userId: Long,
        limit: Int,
        offset: Long
    ): PaginatedResult<Notification>

    suspend fun countUnread(userId: Long): Long

    suspend fun markAllAsRead(userId: Long): Boolean

    suspend fun markAsRead(notificationId: Long, userId: Long): Boolean

    suspend fun deleteLikeNotification(senderId: Long, postId: Long): Boolean

    suspend fun deleteCommentLikeNotification(senderId: Long, commentId: Long): Boolean
}