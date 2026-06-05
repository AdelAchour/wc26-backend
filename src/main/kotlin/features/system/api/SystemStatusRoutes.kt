package com.adel.features.system.api

import com.adel.features.system.service.SystemConfigService
import com.adel.plugins.JWT_ADMIN_AUTH_NAME
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateSystemConfigRequest(
    @SerialName("min_android_version")
    val minAndroidVersion: Int? = null,
    @SerialName("maintenance_mode")
    val maintenanceMode: Boolean? = null,
    @SerialName("android_update_url")
    val androidUpdateUrl: String? = null
)

fun Route.systemStatusRoutes(service: SystemConfigService) {
    get("system-status") {
        val config = service.getSystemConfig()
        call.respond(
            SystemStatusResponse(
                minAndroidVersion = config.minAndroidVersion,
                maintenanceMode = config.maintenanceMode,
                androidUpdateUrl = config.androidUpdateUrl
            )
        )
    }

    authenticate(JWT_ADMIN_AUTH_NAME) {
        patch("/admin/system-status") {
            val request = call.receive<UpdateSystemConfigRequest>()

            // Validation: Ensure at least one field is provided for update
            if (request.minAndroidVersion == null && request.maintenanceMode == null && request.androidUpdateUrl == null) {
                return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "At least one config field (min_android_version, maintenance_mode, android_update_url) must be provided")
                )
            }

            val updatedConfig = service.updateSystemConfig(
                minAndroidVersion = request.minAndroidVersion,
                maintenanceMode = request.maintenanceMode,
                androidUpdateUrl = request.androidUpdateUrl
            )

            if (updatedConfig != null) {
                call.respond(
                    SystemStatusResponse(
                        minAndroidVersion = updatedConfig.minAndroidVersion,
                        maintenanceMode = updatedConfig.maintenanceMode,
                        androidUpdateUrl = updatedConfig.androidUpdateUrl
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update system config")
                )
            }
        }
    }
}