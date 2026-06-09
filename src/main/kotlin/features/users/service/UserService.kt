package com.adel.features.users.service

import com.adel.features.users.data.UserRepository
import com.adel.features.users.data.UpdateProfileParams
import com.adel.features.users.domain.User

sealed interface UpdateProfileResult {
    data class Success(val user: User) : UpdateProfileResult
    object UserNotFound : UpdateProfileResult
    object DisplayNameTooShort : UpdateProfileResult
    object DisplayNameTooLong : UpdateProfileResult
    object AvatarUrlBlank : UpdateProfileResult
    object BioTooLong : UpdateProfileResult
}

class UserService(
    private val repository: UserRepository,
) {
    suspend fun getUser(id: Long): User? = repository.findById(id)

    suspend fun updateProfile(id: Long, params: UpdateProfileParams): UpdateProfileResult {
        if (params.hasDisplayName) {
            val displayName = params.displayName
            if (displayName == null || displayName.length < 2) {
                return UpdateProfileResult.DisplayNameTooShort
            }
            if (displayName.length > 50) {
                return UpdateProfileResult.DisplayNameTooLong
            }
        }
        if (params.hasAvatarUrl) {
            val avatarUrl = params.avatarUrl
            if (avatarUrl.isNullOrBlank()) {
                return UpdateProfileResult.AvatarUrlBlank
            }
        }
        if (params.hasBio) {
            val bio = params.bio
            if (bio != null && bio.length > 100) {
                return UpdateProfileResult.BioTooLong
            }
        }

        val updatedUser = repository.updateProfile(id, params)
            ?: return UpdateProfileResult.UserNotFound

        return UpdateProfileResult.Success(updatedUser)
    }

    suspend fun getAllUsers(): List<User> = repository.findAll()
}