package com.adel.features.posts.data

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.posts.domain.Post
import com.adel.features.posts.domain.PostWithAuthor

interface PostRepository {
    suspend fun findByIdWithAuthor(id: Long): PostWithAuthor?

    suspend fun findByMatchWithAuthor(
        matchId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor>

    suspend fun findAllWithAuthor(
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor>

    suspend fun create(userId: Long, matchId: Long, content: String): Post
    suspend fun findById(id: Long): Post?
    suspend fun delete(id: Long): Boolean
}