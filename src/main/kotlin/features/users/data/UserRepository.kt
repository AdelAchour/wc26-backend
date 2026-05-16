package com.adel.features.users.data

import com.adel.features.users.domain.User

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(
        email: String,
        passwordHash: String,
        displayName: String,
    ): User
    suspend fun emailExists(email: String): Boolean
}