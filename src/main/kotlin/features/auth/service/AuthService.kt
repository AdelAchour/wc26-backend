package com.adel.features.auth.service

import com.adel.common.security.PasswordHasher
import com.adel.features.users.data.UserRepository
import com.adel.features.users.domain.User

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) {
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): RegisterResult {
        // 1. Normalize email (lowercase + trim). CITEXT handles case-insensitive
        //    comparisons, but normalizing here keeps DB content consistent.
        val normalizedEmail = email.trim().lowercase()
        val cleanDisplayName = displayName.trim()

        // 2. Validate input (length, format basics; DB constraints catch the rest)
        if (!isValidEmail(normalizedEmail)) {
            return RegisterResult.InvalidEmail
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return RegisterResult.PasswordTooShort
        }
        if (cleanDisplayName.length < MIN_DISPLAY_NAME_LENGTH) {
            return RegisterResult.DisplayNameTooShort
        }

        // 3. Check for existing user
        if (userRepository.emailExists(normalizedEmail)) {
            return RegisterResult.EmailAlreadyTaken
        }

        // 4. Hash password and create user
        val passwordHash = passwordHasher.hash(password)
        val user = userRepository.create(
            email = normalizedEmail,
            passwordHash = passwordHash,
            displayName = cleanDisplayName,
        )

        return RegisterResult.Success(user)
    }

    private fun isValidEmail(email: String): Boolean =
        EMAIL_REGEX.matches(email)

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_DISPLAY_NAME_LENGTH = 2
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

sealed interface RegisterResult {
    data class Success(val user: User) : RegisterResult
    data object InvalidEmail : RegisterResult
    data object PasswordTooShort : RegisterResult
    data object DisplayNameTooShort : RegisterResult
    data object EmailAlreadyTaken : RegisterResult
}