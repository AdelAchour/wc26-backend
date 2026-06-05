package com.adel.features.system.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object SystemConfigTable : Table("system_config") {
    val id = integer("id").default(1)
    val minAndroidVersion = integer("min_android_version")
    val maintenanceMode = bool("maintenance_mode")
    val androidUpdateUrl = text("android_update_url")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}