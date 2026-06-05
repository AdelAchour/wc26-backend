package com.adel.features.system.di

import com.adel.features.system.data.SystemConfigRepository
import com.adel.features.system.data.SystemConfigRepositoryImpl
import com.adel.features.system.service.SystemConfigService

class SystemComponent {
    val repository: SystemConfigRepository by lazy { SystemConfigRepositoryImpl() }
    val service: SystemConfigService by lazy { SystemConfigService(repository) }
}