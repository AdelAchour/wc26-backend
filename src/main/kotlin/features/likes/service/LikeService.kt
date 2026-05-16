package com.adel.features.likes.service

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.likes.data.LikeRepository
import com.adel.features.posts.data.PostRepository
import com.adel.features.posts.domain.PostWithAuthor
import com.adel.features.users.domain.User

class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
) {

    suspend fun likePost(userId: Long, postId: Long): LikeResult {
        // Verify the post exists before counting it
        if (postRepository.findById(postId) == null) {
            return LikeResult.PostNotFound
        }

        // Idempotent: returns true if newly inserted, false if already liked
        val newlyCreated = likeRepository.create(userId, postId)
        if (newlyCreated) {
            postRepository.incrementLikeCount(postId)
        }

        return LikeResult.Success
    }

    suspend fun unlikePost(userId: Long, postId: Long): LikeResult {
        if (postRepository.findById(postId) == null) {
            return LikeResult.PostNotFound
        }

        val deleted = likeRepository.delete(userId, postId)
        if (deleted) {
            postRepository.decrementLikeCount(postId)
        }

        return LikeResult.Success
    }

    suspend fun listLikersForPost(
        postId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User> = likeRepository.findUsersWhoLikedPost(postId, cursor, limit)

    suspend fun listPostsLikedBy(
        userId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> = likeRepository.findPostsLikedByUser(userId, cursor, limit)

    suspend fun whichArePostsLikedBy(userId: Long, postIds: List<Long>): Set<Long> =
        likeRepository.areLikedBy(userId, postIds)
}

sealed interface LikeResult {
    data object Success : LikeResult
    data object PostNotFound : LikeResult
}