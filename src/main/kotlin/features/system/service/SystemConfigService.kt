package com.adel.features.system.service

import com.adel.features.system.data.SystemConfigRepository
import com.adel.features.system.domain.SystemConfig
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime

class SystemConfigService(private val repository: SystemConfigRepository) {
    private val logger = LoggerFactory.getLogger(SystemConfigService::class.java)

    private val fallbackConfig = SystemConfig(
        minAndroidVersion = 1,
        maintenanceMode = false,
        androidUpdateUrl = "https://play.google.com/store/apps/details?id=com.adel.wc26",
        updatedAt = OffsetDateTime.now()
    )

    suspend fun getSystemConfig(): SystemConfig {
        return try {
            repository.getSystemConfig() ?: run {
                logger.warn("System config not found in database, falling back to default values")
                fallbackConfig
            }
        } catch (e: Exception) {
            logger.error("Error retrieving system config from database, using fallback", e)
            fallbackConfig
        }
    }

    suspend fun updateSystemConfig(
        minAndroidVersion: Int?,
        maintenanceMode: Boolean?,
        androidUpdateUrl: String?
    ): SystemConfig? {
        return try {
            repository.updateSystemConfig(minAndroidVersion, maintenanceMode, androidUpdateUrl)
        } catch (e: Exception) {
            logger.error("Failed to update system config in database", e)
            null
        }
    }
}