package com.adel.features.comments.service

import com.adel.common.pagination.PaginatedResult
import com.adel.features.comments.data.CommentRepository
import com.adel.features.comments.domain.Comment
import com.adel.features.comments.domain.CommentWithAuthor
import com.adel.features.notifications.domain.NotificationType
import com.adel.features.notifications.service.NotificationService
import com.adel.features.posts.data.PostRepository

class CommentService(
    private val repository: CommentRepository,
    private val postRepository: PostRepository,
    private val notificationService: NotificationService
) {
    suspend fun getComment(id: Long): CommentWithAuthor? = repository.findByIdWithAuthor(id)

    suspend fun listForPost(
        postId: Long,
        limit: Int,
        offset: Long,
    ): PaginatedResult<CommentWithAuthor> =
        repository.listByPost(postId, limit, offset)

    suspend fun createComment(
        userId: Long,
        postId: Long,
        content: String,
    ): CreateCommentResult {
        val cleanContent = content.trim()
        if (cleanContent.isEmpty()) return CreateCommentResult.ContentEmpty
        if (cleanContent.length > MAX_CONTENT_LENGTH) return CreateCommentResult.ContentTooLong

        val post = postRepository.findById(postId) ?: return CreateCommentResult.PostNotFound

        val comment = repository.create(userId, postId, cleanContent)
        postRepository.incrementCommentCount(postId)

        // Trigger REPLY_POST notification
        notificationService.createNotification(
            senderId = userId,
            receiverId = post.userId,
            type = NotificationType.REPLY_POST,
            postId = postId,
            commentId = comment.id
        )

        return CreateCommentResult.Success(comment)
    }

    suspend fun deleteComment(commentId: Long, requesterId: Long): DeleteCommentResult {
        val comment = repository.findById(commentId)
            ?: return DeleteCommentResult.NotFound

        if (comment.userId != requesterId) {
            return DeleteCommentResult.Forbidden
        }

        repository.delete(commentId)
        postRepository.decrementCommentCount(comment.postId)

        return DeleteCommentResult.Success
    }

    companion object {
        const val MAX_CONTENT_LENGTH = 300
    }
}

sealed interface CreateCommentResult {
    data class Success(val comment: Comment) : CreateCommentResult
    data object ContentEmpty : CreateCommentResult
    data object ContentTooLong : CreateCommentResult
    data object PostNotFound : CreateCommentResult
}

sealed interface DeleteCommentResult {
    data object Success : DeleteCommentResult
    data object NotFound : DeleteCommentResult
    data object Forbidden : DeleteCommentResult
}