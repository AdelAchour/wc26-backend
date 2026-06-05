package com.adel.features.system.data

import com.adel.features.system.domain.SystemConfig

interface SystemConfigRepository {
    suspend fun getSystemConfig(): SystemConfig?
    suspend fun updateSystemConfig(
        minAndroidVersion: Int?,
        maintenanceMode: Boolean?,
        androidUpdateUrl: String?
    ): SystemConfig?
}