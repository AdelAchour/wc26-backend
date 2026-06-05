package com.adel.features.system.data

import com.adel.common.database.dbQuery
import com.adel.features.system.domain.SystemConfig
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class SystemConfigRepositoryImpl : SystemConfigRepository {

    override suspend fun getSystemConfig(): SystemConfig? = dbQuery {
        SystemConfigTable
            .selectAll()
            .where { SystemConfigTable.id eq 1 }
            .map { it.toSystemConfig() }
            .singleOrNull()
    }

    override suspend fun updateSystemConfig(
        minAndroidVersion: Int?,
        maintenanceMode: Boolean?,
        androidUpdateUrl: String?
    ): SystemConfig? = dbQuery {
        val updatedRows = SystemConfigTable.update({ SystemConfigTable.id eq 1 }) {
            if (minAndroidVersion != null) {
                it[SystemConfigTable.minAndroidVersion] = minAndroidVersion
            }
            if (maintenanceMode != null) {
                it[SystemConfigTable.maintenanceMode] = maintenanceMode
            }
            if (androidUpdateUrl != null) {
                it[SystemConfigTable.androidUpdateUrl] = androidUpdateUrl
            }
            it[SystemConfigTable.updatedAt] = OffsetDateTime.now()
        }

        if (updatedRows > 0) {
            getSystemConfigInternal()
        } else {
            null
        }
    }

    private fun getSystemConfigInternal(): SystemConfig? {
        return SystemConfigTable
            .selectAll()
            .where { SystemConfigTable.id eq 1 }
            .map { it.toSystemConfig() }
            .singleOrNull()
    }

    private fun ResultRow.toSystemConfig(): SystemConfig = SystemConfig(
        minAndroidVersion = this[SystemConfigTable.minAndroidVersion],
        maintenanceMode = this[SystemConfigTable.maintenanceMode],
        androidUpdateUrl = this[SystemConfigTable.androidUpdateUrl],
        updatedAt = this[SystemConfigTable.updatedAt]
    )
}