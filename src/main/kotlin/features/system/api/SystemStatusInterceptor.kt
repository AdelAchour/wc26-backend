package com.adel.features.system.api

import com.adel.features.system.service.SystemConfigService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.path
import io.ktor.server.response.respond

val SystemStatusInterceptor = createApplicationPlugin(
    name = "SystemStatusInterceptor",
    createConfiguration = { SystemStatusInterceptorConfig() }
) {
    val service = pluginConfig.service ?: error("SystemConfigService is required for SystemStatusInterceptor")

    onCall { call ->
        val path = call.request.path()
        // Bypass checks for monitoring/health checks and the status endpoint
        if (path == "/" || path == "/system-status" || path == "/admin/system-status") {
            return@onCall
        }

        val config = service.getSystemConfig()

        // 1. Maintenance Check
        if (config.maintenanceMode) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                MaintenanceResponse(
                    error = "Maintenance mode is active. Please try again later.",
                    maintenanceMode = true
                )
            )
            return@onCall
        }

        // 2. Version Check
        val userAgent = call.request.headers["User-Agent"] ?: ""
        // OkHttp sends "okhttp/x.y.z" by default; Android WebViews send "Android"
        val isMobileAndroid = userAgent.contains("okhttp", ignoreCase = true) ||
                userAgent.contains("Android", ignoreCase = true)

        val appVersionHeader = call.request.headers["X-App-Version"]?.toIntOrNull()

        if (isMobileAndroid) {
            // Mandate the header for mobile clients
            if (appVersionHeader == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "X-App-Version header is required for Android clients.")
                )
                return@onCall
            }
            // Enforce version limit
            if (appVersionHeader < config.minAndroidVersion) {
                call.respond(
                    HttpStatusCode.UpgradeRequired, // 426
                    UpgradeRequiredResponse(
                        error = "An update is required to continue using the application.",
                        androidUpdateUrl = config.androidUpdateUrl,
                        minAndroidVersion = config.minAndroidVersion,
                        currentAndroidVersion = appVersionHeader,
                    )
                )
                return@onCall
            }
        }
        else {
            // Optional: If a non-mobile client (like Postman) decides to send the version header,
            // we still check it just in case.
            if (appVersionHeader != null && appVersionHeader < config.minAndroidVersion) {
                call.respond(
                    HttpStatusCode.UpgradeRequired,
                    UpgradeRequiredResponse(
                        error = "An update is required to continue using the application.",
                        androidUpdateUrl = config.androidUpdateUrl,
                        minAndroidVersion = config.minAndroidVersion,
                        currentAndroidVersion = appVersionHeader,
                    )
                )
                return@onCall
            }
        }
    }
}

class SystemStatusInterceptorConfig {
    var service: SystemConfigService? = null
}