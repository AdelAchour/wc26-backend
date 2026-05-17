package com.adel.features.comments.data

import com.adel.common.pagination.PaginatedResult
import com.adel.features.comments.domain.Comment
import com.adel.features.comments.domain.CommentWithAuthor

interface CommentRepository {
    suspend fun findById(id: Long): Comment?
    suspend fun findByIdWithAuthor(id: Long): CommentWithAuthor?
    suspend fun listByPost(
        postId: Long,
        limit: Int,
        offset: Long,
    ): PaginatedResult<CommentWithAuthor>
    suspend fun create(userId: Long, postId: Long, content: String): Comment
    suspend fun delete(id: Long): Boolean
}