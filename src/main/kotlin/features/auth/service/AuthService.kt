package com.adel.features.auth.service

import com.adel.common.email.EmailService
import com.adel.common.security.JwtService
import com.adel.common.security.PasswordHasher
import com.adel.features.auth.data.PasswordResetRepository
import com.adel.features.users.data.UserRepository
import com.adel.features.users.domain.User

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
    private val passwordResetRepository: PasswordResetRepository,
    private val emailService: EmailService,
) {

    suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String,
    ): RegisterResult {
        val normalizedEmail = email.trim().lowercase()
        val normalizedUsername = username.trim().lowercase()
        val cleanDisplayName = displayName.trim()

        if (!isValidEmail(normalizedEmail)) return RegisterResult.InvalidEmail
        if (!isValidUsername(normalizedUsername)) return RegisterResult.InvalidUsername
        if (password.length < MIN_PASSWORD_LENGTH) return RegisterResult.PasswordTooShort
        if (cleanDisplayName.length < MIN_DISPLAY_NAME_LENGTH) return RegisterResult.DisplayNameTooShort

        if (userRepository.emailExists(normalizedEmail)) return RegisterResult.EmailAlreadyTaken
        if (userRepository.usernameExists(normalizedUsername)) return RegisterResult.UsernameAlreadyTaken

        val passwordHash = passwordHasher.hash(password)
        val user = userRepository.create(
            email = normalizedEmail,
            username = normalizedUsername,
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

    /**
     * Initiates a password reset flow. If the email exists, generates a
     * 6-digit numeric code, stores it with an expiry, and sends it via email.
     *
     * Always succeeds from the caller's perspective to prevent email enumeration.
     */
    suspend fun forgotPassword(email: String) {
        val normalizedEmail = email.trim().lowercase()

        // Only proceed if the user actually exists — but always return success
        val userExists = userRepository.emailExists(normalizedEmail)
        if (!userExists) return

        val code = generateResetCode()
        passwordResetRepository.upsertCode(normalizedEmail, code, CODE_EXPIRY_MINUTES)
        emailService.sendPasswordResetEmailAsync(normalizedEmail, code, CODE_EXPIRY_MINUTES)
    }

    /**
     * Validates the reset code and updates the user's password.
     */
    suspend fun resetPassword(email: String, code: String, newPassword: String): ResetPasswordResult {
        val normalizedEmail = email.trim().lowercase()

        if (newPassword.length < MIN_PASSWORD_LENGTH) return ResetPasswordResult.PasswordTooShort

        val validCode = passwordResetRepository.findValidCode(normalizedEmail, code)
            ?: return ResetPasswordResult.InvalidOrExpiredCode

        val newHash = passwordHasher.hash(newPassword)
        val updated = userRepository.updatePasswordHash(normalizedEmail, newHash)
        if (!updated) return ResetPasswordResult.InvalidOrExpiredCode

        // Code used successfully — delete it so it can't be reused
        passwordResetRepository.deleteByEmail(normalizedEmail)

        return ResetPasswordResult.Success
    }

    private fun generateResetCode(): String =
        (100_000..999_999).random().toString()

    private fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    private fun isValidUsername(username: String): Boolean = USERNAME_REGEX.matches(username)

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_DISPLAY_NAME_LENGTH = 2
        const val CODE_EXPIRY_MINUTES = 10
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        private val USERNAME_REGEX = Regex("^[a-z][a-z0-9_]{2,19}$")
    }
}

sealed interface RegisterResult {
    data class Success(val user: User, val token: String) : RegisterResult
    data object InvalidEmail : RegisterResult
    data object InvalidUsername : RegisterResult
    data object PasswordTooShort : RegisterResult
    data object DisplayNameTooShort : RegisterResult
    data object EmailAlreadyTaken : RegisterResult
    data object UsernameAlreadyTaken : RegisterResult
}

sealed interface LoginResult {
    data class Success(val user: User, val token: String) : LoginResult
    data object InvalidCredentials : LoginResult
}

sealed interface ResetPasswordResult {
    data object Success : ResetPasswordResult
    data object PasswordTooShort : ResetPasswordResult
    data object InvalidOrExpiredCode : ResetPasswordResult
}