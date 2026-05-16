package com.adel.features.likes.data

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.posts.domain.PostWithAuthor
import com.adel.features.users.domain.User

interface LikeRepository {
    suspend fun exists(userId: Long, postId: Long): Boolean
    suspend fun create(userId: Long, postId: Long): Boolean   // returns true if newly created
    suspend fun delete(userId: Long, postId: Long): Boolean   // returns true if a row was deleted
    suspend fun findUsersWhoLikedPost(
        postId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User>
    suspend fun findPostsLikedByUser(
        userId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor>
    suspend fun areLikedBy(userId: Long, postIds: List<Long>): Set<Long>
}