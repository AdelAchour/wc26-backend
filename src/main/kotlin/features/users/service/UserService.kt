package com.adel.features.users.service

import com.adel.features.users.data.UserRepository
import com.adel.features.users.domain.User

class UserService(
    private val repository: UserRepository,
) {
    suspend fun getUser(id: Long): User? = repository.findById(id)
}