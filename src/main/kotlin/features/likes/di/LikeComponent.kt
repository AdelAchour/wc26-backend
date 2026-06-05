package com.adel.features.likes.di

import com.adel.features.likes.data.LikeRepository
import com.adel.features.likes.data.LikeRepositoryImpl
import com.adel.features.likes.service.LikeService
import com.adel.features.notifications.service.NotificationService
import com.adel.features.posts.data.PostRepository

class LikeComponent(
    postRepository: PostRepository,
    private val notificationService: NotificationService
) {
    val repository: LikeRepository by lazy { LikeRepositoryImpl() }
    val service: LikeService by lazy { LikeService(repository, postRepository, notificationService) }
}