package com.adel.plugins

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import io.ktor.server.application.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

private val logger = LoggerFactory.getLogger("FirebaseConfig")

fun Application.configureFirebase() {
    val keyPath = environment.config.propertyOrNull("firebase.serviceAccountKeyPath")?.getString() ?: "service-account-key.json"
    val file = File(keyPath)
    if (file.exists()) {
        try {
            FileInputStream(file).use { serviceAccount ->
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                }
                logger.info("[FCM] Firebase successfully initialized. Push notifications enabled.")
            }
        } catch (e: Exception) {
            logger.error("[FCM] Error initializing Firebase Admin SDK", e)
        }
    } else {
        logger.warn("[FCM] Warning: Firebase credentials file not found. Push notifications will be disabled for this environment.")
    }
}
