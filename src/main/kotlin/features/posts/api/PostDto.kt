package com.adel.features.posts.api

import com.adel.features.posts.domain.PostWithAuthor
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val matchId: Long,
    val author: PostAuthorDto,
    val content: String,
    val likeCount: Int,
    val likedByCurrentUser: Boolean,
    val createdAt: String,
)

@Serializable
data class PostAuthorDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
)

@Serializable
data class CreatePostRequest(
    val content: String,
)

/**
 * Convert a PostWithAuthor to its wire-format DTO.
 *
 * @param likedByCurrentUser whether the authenticated user has liked this post.
 *        Always false for unauthenticated requests, or when the like info hasn't
 *        been resolved yet. Wired up properly in Stage 3 when likes are added.
 */
fun PostWithAuthor.toDto(likedByCurrentUser: Boolean = false): PostDto = PostDto(
    id = post.id,
    matchId = post.matchId,
    author = PostAuthorDto(
        id = author.id,
        username = author.username,
        displayName = author.displayName,
        avatarUrl = author.avatarUrl,
    ),
    content = post.content,
    likeCount = post.likeCount,
    likedByCurrentUser = likedByCurrentUser,
    createdAt = post.createdAt.toString(),
)