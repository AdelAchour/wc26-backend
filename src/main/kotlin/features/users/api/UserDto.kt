package com.adel.features.users.api

import com.adel.features.users.domain.User
import kotlinx.serialization.Serializable

/**
 * Private user view — only returned for the user's own profile (GET /auth/me).
 * Includes sensitive fields like email and role.
 */
@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val createdAt: String,
)

/**
 * Public user view — safe to return for any user lookup.
 * Excludes email, role, and any other private data.
 */
@Serializable
data class UserPublicDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val createdAt: String,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    role = role.value,
    createdAt = createdAt.toString(),
)

fun User.toPublicDto(): UserPublicDto = UserPublicDto(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    createdAt = createdAt.toString(),
)