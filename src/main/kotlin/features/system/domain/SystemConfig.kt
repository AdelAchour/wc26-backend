package com.adel.features.system.domain

import java.time.OffsetDateTime

data class SystemConfig(
    val minAndroidVersion: Int,
    val maintenanceMode: Boolean,
    val androidUpdateUrl: String,
    val updatedAt: OffsetDateTime
)