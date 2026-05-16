package com.adel.features.users.api

import com.adel.features.users.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val createdAt: String,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    role = role.value,
    createdAt = createdAt.toString(),
)