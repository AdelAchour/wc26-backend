package com.adel.features.notifications.api

import com.adel.common.pagination.toDto
import com.adel.common.security.requireUserId
import com.adel.features.notifications.data.UserPushTokenRepository
import com.adel.features.notifications.service.NotificationService
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.notificationRoutes(service: NotificationService) {

    authenticate(JWT_AUTH_NAME) {
        route("/notifications") {

            // Get user's notifications feed
            get {
                val userId = call.requireUserId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull()
                    ?.coerceAtLeast(0)
                    ?: 0L

                val result = service.getNotificationsForUser(userId, limit, offset)
                call.respond(result.toDto { it.toDto() })
            }

            // Get unread notifications count
            get("/unread-count") {
                val userId = call.requireUserId()
                val unreadCount = service.getUnreadCount(userId)
                call.respond(UnreadCountDto(unreadCount))
            }

            // Mark all notifications as read
            post("/read") {
                val userId = call.requireUserId()
                service.markAllAsRead(userId)
                call.respond(HttpStatusCode.NoContent)
            }

            // Mark a single notification as read
            patch("/{id}/read") {
                val notificationId = call.parameters["id"]?.toLongOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid notification id"))
                val userId = call.requireUserId()

                val success = service.markAsRead(notificationId, userId)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Notification not found"))
                }
            }
        }
    }
}

fun Route.pushTokenRoutes(pushTokenRepository: UserPushTokenRepository) {
    authenticate(JWT_AUTH_NAME) {
        route("/users/push-token") {
            post {
                val userId = call.requireUserId()
                val request = call.receive<PushTokenRequest>()
                if (request.pushToken.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "pushToken cannot be blank"))
                }
                pushTokenRepository.registerToken(userId, request.pushToken)
                call.respond(HttpStatusCode.OK, mapOf("status" to "registered"))
            }

            delete {
                val userId = call.requireUserId()
                val request = call.receive<PushTokenRequest>()
                if (request.pushToken.isBlank()) {
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "pushToken cannot be blank"))
                }
                pushTokenRepository.unregisterToken(userId, request.pushToken)
                call.respond(HttpStatusCode.OK, mapOf("status" to "unregistered"))
            }
        }
    }
}

@Serializable
data class PushTokenRequest(
    val pushToken: String
)