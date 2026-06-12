package com.adel.features.comments.data

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.users.domain.User

interface CommentLikeRepository {
    suspend fun exists(userId: Long, commentId: Long): Boolean
    suspend fun create(userId: Long, commentId: Long): Boolean
    suspend fun delete(userId: Long, commentId: Long): Boolean
    suspend fun areLikedBy(userId: Long, commentIds: List<Long>): Set<Long>
    suspend fun findUsersWhoLikedComment(
        commentId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User>
}