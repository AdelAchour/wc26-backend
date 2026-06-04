package com.adel.features.users.domain

import java.time.OffsetDateTime

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val passwordHash: String,   // never leaves the data/service boundary
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val role: UserRole,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)