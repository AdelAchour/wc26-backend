package com.adel.features.users.di

import com.adel.features.users.data.UserRepository
import com.adel.features.users.data.UserRepositoryImpl
import com.adel.features.users.service.UserService

class UserComponent {
    val repository: UserRepository by lazy { UserRepositoryImpl() }
    val service: UserService by lazy { UserService(repository) }
}