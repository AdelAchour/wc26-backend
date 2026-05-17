package com.adel.features.posts.domain

import com.adel.features.users.domain.User
import java.time.OffsetDateTime

data class Post(
    val id: Long,
    val userId: Long,
    val matchId: Long,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

/**
 * Post enriched with its author, for feed/detail views where the API
 * needs to display the author's name and avatar.
 */
data class PostWithAuthor(
    val post: Post,
    val author: User,
)