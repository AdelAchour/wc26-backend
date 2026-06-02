package com.adel.features.posts.api

import com.adel.features.matches.api.MatchDto
import com.adel.features.posts.domain.PostWithAuthor
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val matchId: Long,
    val match: MatchDto? = null,
    val author: PostAuthorDto,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
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
 * @param match the nested match object if this post is being served in a mixed feed.
 */
fun PostWithAuthor.toDto(
    likedByCurrentUser: Boolean = false,
    match: MatchDto? = null,
): PostDto = PostDto(
    id = post.id,
    matchId = post.matchId,
    match = match,
    author = PostAuthorDto(
        id = author.id,
        username = author.username,
        displayName = author.displayName,
        avatarUrl = author.avatarUrl,
    ),
    content = post.content,
    likeCount = post.likeCount,
    commentCount = post.commentCount,
    likedByCurrentUser = likedByCurrentUser,
    createdAt = post.createdAt.toString(),
)