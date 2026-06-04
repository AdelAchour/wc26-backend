package com.adel.features.users.data

import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole

data class UpdateProfileParams(
    val displayName: String? = null,
    val hasDisplayName: Boolean = false,
    val avatarUrl: String? = null,
    val hasAvatarUrl: Boolean = false,
    val bio: String? = null,
    val hasBio: Boolean = false,
    val role: UserRole? = null,
    val hasRole: Boolean = false,
)

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(
        email: String,
        username: String,
        passwordHash: String,
        displayName: String,
    ): User
    suspend fun emailExists(email: String): Boolean
    suspend fun usernameExists(username: String): Boolean
    suspend fun updateProfile(id: Long, params: UpdateProfileParams): User?
}