package com.adel.features.system.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemStatusResponse(
    @SerialName("min_android_version")
    val minAndroidVersion: Int,
    @SerialName("maintenance_mode")
    val maintenanceMode: Boolean,
    @SerialName("android_update_url")
    val androidUpdateUrl: String
)

@Serializable
data class UpgradeRequiredResponse(
    val error: String,
    @SerialName("android_update_url")
    val androidUpdateUrl: String,
    @SerialName("min_android_version")
    val minAndroidVersion: Int,
    @SerialName("current_android_version")
    val currentAndroidVersion: Int
)

@Serializable
data class MaintenanceResponse(
    val error: String,
    @SerialName("maintenance_mode")
    val maintenanceMode: Boolean
)