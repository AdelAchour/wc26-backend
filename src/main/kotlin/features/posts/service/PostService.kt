package com.adel.features.posts.service

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.matches.data.MatchRepository
import com.adel.features.posts.data.PostRepository
import com.adel.features.posts.domain.Post
import com.adel.features.posts.domain.PostWithAuthor

class PostService(
    private val repository: PostRepository,
    private val matchRepository: MatchRepository,
) {
    suspend fun getPost(id: Long): PostWithAuthor? =
        repository.findByIdWithAuthor(id)

    suspend fun listPostsForMatch(
        matchId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> =
        repository.findByMatchWithAuthor(matchId, cursor, limit)

    suspend fun listAllPosts(
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> =
        repository.findAllWithAuthor(cursor, limit)

    suspend fun createPost(
        userId: Long,
        matchId: Long,
        content: String,
    ): CreatePostResult {
        val cleanContent = content.trim()
        if (cleanContent.isEmpty()) return CreatePostResult.ContentEmpty
        if (cleanContent.length > MAX_CONTENT_LENGTH) return CreatePostResult.ContentTooLong

        if (matchRepository.findById(matchId) == null) {
            return CreatePostResult.MatchNotFound
        }

        val post = repository.create(userId, matchId, cleanContent)
        return CreatePostResult.Success(post)
    }

    suspend fun deletePost(postId: Long, requesterId: Long): DeletePostResult {
        val post = repository.findById(postId)
            ?: return DeletePostResult.NotFound

        if (post.userId != requesterId) {
            return DeletePostResult.Forbidden
        }

        repository.delete(postId)
        return DeletePostResult.Success
    }

    companion object {
        const val MAX_CONTENT_LENGTH = 500
    }
}

sealed interface CreatePostResult {
    data class Success(val post: Post) : CreatePostResult
    data object ContentEmpty : CreatePostResult
    data object ContentTooLong : CreatePostResult
    data object MatchNotFound : CreatePostResult
}

sealed interface DeletePostResult {
    data object Success : DeletePostResult
    data object NotFound : DeletePostResult
    data object Forbidden : DeletePostResult
}