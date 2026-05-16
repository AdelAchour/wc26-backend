package com.adel.features.auth.di

import com.adel.common.security.PasswordHasher
import com.adel.features.auth.service.AuthService
import com.adel.features.users.data.UserRepository

class AuthComponent(
    userRepository: UserRepository,
) {
    private val passwordHasher: PasswordHasher by lazy { PasswordHasher() }
    val service: AuthService by lazy { AuthService(userRepository, passwordHasher) }
}