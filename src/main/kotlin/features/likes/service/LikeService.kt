package com.adel.features.likes.service

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.likes.data.LikeRepository
import com.adel.features.notifications.domain.NotificationType
import com.adel.features.notifications.service.NotificationService
import com.adel.features.posts.data.PostRepository
import com.adel.features.posts.domain.PostWithAuthor
import com.adel.features.users.domain.User

class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
    private val notificationService: NotificationService
) {

    suspend fun likePost(userId: Long, postId: Long): LikeResult {
        val post = postRepository.findById(postId) ?: return LikeResult.PostNotFound
        val newlyCreated = likeRepository.create(userId, postId)
        if (newlyCreated) {
            postRepository.incrementLikeCount(postId)

            // Trigger LIKE_POST notification
            notificationService.createNotification(
                senderId = userId,
                receiverId = post.userId,
                type = NotificationType.LIKE_POST,
                postId = postId
            )
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

            // Clean up the notification if unliked
            notificationService.deleteLikeNotification(senderId = userId, postId = postId)
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