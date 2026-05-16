package com.adel.features.auth.service

import com.adel.common.security.JwtService
import com.adel.common.security.PasswordHasher
import com.adel.features.users.data.UserRepository
import com.adel.features.users.domain.User

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) {

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): RegisterResult {
        val normalizedEmail = email.trim().lowercase()
        val cleanDisplayName = displayName.trim()

        if (!isValidEmail(normalizedEmail)) return RegisterResult.InvalidEmail
        if (password.length < MIN_PASSWORD_LENGTH) return RegisterResult.PasswordTooShort
        if (cleanDisplayName.length < MIN_DISPLAY_NAME_LENGTH) return RegisterResult.DisplayNameTooShort
        if (userRepository.emailExists(normalizedEmail)) return RegisterResult.EmailAlreadyTaken

        val passwordHash = passwordHasher.hash(password)
        val user = userRepository.create(
            email = normalizedEmail,
            passwordHash = passwordHash,
            displayName = cleanDisplayName,
        )

        val token = jwtService.generateToken(user)
        return RegisterResult.Success(user, token)
    }

    suspend fun login(email: String, password: String): LoginResult {
        val normalizedEmail = email.trim().lowercase()

        val user = userRepository.findByEmail(normalizedEmail)
            ?: return LoginResult.InvalidCredentials

        val passwordMatches = passwordHasher.verify(password, user.passwordHash)
        if (!passwordMatches) return LoginResult.InvalidCredentials

        val token = jwtService.generateToken(user)
        return LoginResult.Success(user, token)
    }

    private fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_DISPLAY_NAME_LENGTH = 2
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

sealed interface RegisterResult {
    data class Success(val user: User, val token: String) : RegisterResult
    data object InvalidEmail : RegisterResult
    data object PasswordTooShort : RegisterResult
    data object DisplayNameTooShort : RegisterResult
    data object EmailAlreadyTaken : RegisterResult
}

sealed interface LoginResult {
    data class Success(val user: User, val token: String) : LoginResult
    data object InvalidCredentials : LoginResult
}