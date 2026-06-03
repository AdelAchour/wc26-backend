package com.adel.features.comments.data

interface CommentLikeRepository {
    suspend fun exists(userId: Long, commentId: Long): Boolean
    suspend fun create(userId: Long, commentId: Long): Boolean
    suspend fun delete(userId: Long, commentId: Long): Boolean
    suspend fun areLikedBy(userId: Long, commentIds: List<Long>): Set<Long>
}