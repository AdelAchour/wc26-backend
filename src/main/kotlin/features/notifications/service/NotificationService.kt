package com.adel.features.notifications.service

import com.adel.common.pagination.PaginatedResult
import com.adel.features.notifications.data.NotificationRepository
import com.adel.features.notifications.domain.Notification
import com.adel.features.notifications.domain.NotificationType

class NotificationService(
    private val repository: NotificationRepository
) {
    suspend fun createNotification(
        senderId: Long,
        receiverId: Long,
        type: NotificationType,
        postId: Long,
        commentId: Long? = null
    ) {
        // Prevent self-notifications
        if (senderId == receiverId) return

        repository.create(
            senderId = senderId,
            receiverId = receiverId,
            type = type,
            postId = postId,
            commentId = commentId
        )
    }

    suspend fun getNotificationsForUser(
        userId: Long,
        limit: Int,
        offset: Long
    ): PaginatedResult<Notification> =
        repository.findNotificationsForUser(userId, limit, offset)

    suspend fun getUnreadCount(userId: Long): Long =
        repository.countUnread(userId)

    suspend fun markAllAsRead(userId: Long): Boolean =
        repository.markAllAsRead(userId)

    suspend fun markAsRead(notificationId: Long, userId: Long): Boolean =
        repository.markAsRead(notificationId, userId)

    suspend fun deleteLikeNotification(senderId: Long, postId: Long) =
        repository.deleteLikeNotification(senderId, postId)

    suspend fun deleteCommentLikeNotification(senderId: Long, commentId: Long) =
        repository.deleteCommentLikeNotification(senderId, commentId)
}