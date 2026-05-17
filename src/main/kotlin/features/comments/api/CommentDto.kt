package com.adel.features.comments.api

import com.adel.features.comments.domain.CommentWithAuthor
import com.adel.features.posts.api.PostAuthorDto
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long,
    val postId: Long,
    val author: PostAuthorDto,  // reuse the existing public-author DTO
    val content: String,
    val createdAt: String,
)

@Serializable
data class CreateCommentRequest(
    val content: String,
)

fun CommentWithAuthor.toDto(): CommentDto = CommentDto(
    id = comment.id,
    postId = comment.postId,
    author = PostAuthorDto(
        id = author.id,
        username = author.username,
        displayName = author.displayName,
        avatarUrl = author.avatarUrl,
    ),
    content = comment.content,
    createdAt = comment.createdAt.toString(),
)