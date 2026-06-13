package com.adel.features.notifications.di

import com.adel.features.notifications.data.NotificationRepository
import com.adel.features.notifications.data.NotificationRepositoryImpl
import com.adel.features.notifications.data.UserPushTokenRepository
import com.adel.features.notifications.data.UserPushTokenRepositoryImpl
import com.adel.features.notifications.service.FcmService
import com.adel.features.notifications.service.NotificationService

class NotificationComponent {
    val repository: NotificationRepository by lazy { NotificationRepositoryImpl() }
    val pushTokenRepository: UserPushTokenRepository by lazy { UserPushTokenRepositoryImpl() }
    val fcmService: FcmService by lazy { FcmService(pushTokenRepository) }
    val service: NotificationService by lazy { NotificationService(repository, fcmService) }
}