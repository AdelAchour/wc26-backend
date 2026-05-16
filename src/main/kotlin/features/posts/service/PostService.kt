package com.adel.features.posts.service

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.posts.data.PostRepository
import com.adel.features.posts.domain.PostWithAuthor

class PostService(
    private val repository: PostRepository,
) {
    suspend fun getPost(id: Long): PostWithAuthor? =
        repository.findByIdWithAuthor(id)

    suspend fun listPostsForMatch(
        matchId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> =
        repository.findByMatchWithAuthor(matchId, cursor, limit)
}