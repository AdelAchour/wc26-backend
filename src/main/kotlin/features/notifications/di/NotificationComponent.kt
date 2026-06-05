package com.adel.features.notifications.di

import com.adel.features.notifications.data.NotificationRepository
import com.adel.features.notifications.data.NotificationRepositoryImpl
import com.adel.features.notifications.service.NotificationService

class NotificationComponent {
    val repository: NotificationRepository by lazy { NotificationRepositoryImpl() }
    val service: NotificationService by lazy { NotificationService(repository) }
}