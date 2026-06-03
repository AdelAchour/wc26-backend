package com.adel.features.auth.api

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val username: String,
    val displayName: String,
)

@Serializable
data class UpdateAvatarRequest(
    val avatarUrl: String
)