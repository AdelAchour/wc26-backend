package com.adel.features.auth.di

import com.adel.common.email.EmailService
import com.adel.common.security.JwtService
import com.adel.common.security.PasswordHasher
import com.adel.config.JwtConfig
import com.adel.features.auth.data.PasswordResetRepository
import com.adel.features.auth.data.PasswordResetRepositoryImpl
import com.adel.features.auth.service.AuthService
import com.adel.features.users.data.UserRepository

class AuthComponent(
    userRepository: UserRepository,
    jwtConfig: JwtConfig,
    emailService: EmailService,
) {
    private val passwordHasher: PasswordHasher by lazy { PasswordHasher() }
    private val passwordResetRepository: PasswordResetRepository by lazy { PasswordResetRepositoryImpl() }
    val jwtService: JwtService by lazy { JwtService(jwtConfig) }
    val service: AuthService by lazy {
        AuthService(userRepository, passwordHasher, jwtService, passwordResetRepository, emailService)
    }
}