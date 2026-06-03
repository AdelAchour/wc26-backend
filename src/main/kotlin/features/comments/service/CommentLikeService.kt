package com.adel.features.comments.service

import com.adel.features.comments.data.CommentLikeRepository
import com.adel.features.comments.data.CommentRepository

class CommentLikeService(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentRepository: CommentRepository,
) {

    suspend fun likeComment(userId: Long, commentId: Long): CommentLikeResult {
        if (commentRepository.findById(commentId) == null) {
            return CommentLikeResult.CommentNotFound
        }

        val newlyCreated = commentLikeRepository.create(userId, commentId)
        if (newlyCreated) {
            commentRepository.incrementLikeCount(commentId)
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
        }

        return CommentLikeResult.Success
    }

    suspend fun whichAreCommentsLikedBy(userId: Long, commentIds: List<Long>): Set<Long> =
        commentLikeRepository.areLikedBy(userId, commentIds)
}

sealed interface CommentLikeResult {
    data object Success : CommentLikeResult
    data object CommentNotFound : CommentLikeResult
}