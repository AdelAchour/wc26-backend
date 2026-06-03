package com.adel.features.comments.domain


import com.adel.features.users.domain.User
import java.time.OffsetDateTime

data class Comment(
    val id: Long,
    val userId: Long,
    val postId: Long,
    val content: String,
    val likeCount: Int,
    val createdAt: OffsetDateTime,
)

/**
 * Comment enriched with its author, for list views that need to display
 * the commenter's name and avatar.
 */
data class CommentWithAuthor(
    val comment: Comment,
    val author: User,
)