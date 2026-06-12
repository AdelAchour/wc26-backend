package com.adel.features.comments.service

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.comments.data.CommentLikeRepository
import com.adel.features.comments.data.CommentRepository
import com.adel.features.notifications.domain.NotificationType
import com.adel.features.notifications.service.NotificationService
import com.adel.features.users.domain.User

class CommentLikeService(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentRepository: CommentRepository,
    private val notificationService: NotificationService
) {

    suspend fun likeComment(userId: Long, commentId: Long): CommentLikeResult {
        val comment = commentRepository.findById(commentId) ?: return CommentLikeResult.CommentNotFound

        val newlyCreated = commentLikeRepository.create(userId, commentId)
        if (newlyCreated) {
            commentRepository.incrementLikeCount(commentId)

            // Trigger LIKE_COMMENT notification
            notificationService.createNotification(
                senderId = userId,
                receiverId = comment.userId,
                type = NotificationType.LIKE_COMMENT,
                postId = comment.postId,
                commentId = commentId
            )
        }

        return CommentLikeResult.Success
    }

    suspend fun unlikeComment(userId: Long, commentId: Long): CommentLikeResult {
        if (commentRepository.findById(commentId) == null) {
            return CommentLikeResult.CommentNotFound
        }

        val deleted = commentLikeRepository.delete(userId, commentId)
        if (deleted) {
            commentRepository.decrementLikeCount(commentId)

            // Clean up the notification if unliked
            notificationService.deleteCommentLikeNotification(senderId = userId, commentId = commentId)
        }

        return CommentLikeResult.Success
    }

    suspend fun whichAreCommentsLikedBy(userId: Long, commentIds: List<Long>): Set<Long> =
        commentLikeRepository.areLikedBy(userId, commentIds)

    suspend fun listLikersForComment(
        commentId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User> = commentLikeRepository.findUsersWhoLikedComment(commentId, cursor, limit)
}

sealed interface CommentLikeResult {
    data object Success : CommentLikeResult
    data object CommentNotFound : CommentLikeResult
}