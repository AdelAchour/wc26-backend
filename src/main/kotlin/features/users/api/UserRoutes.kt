package com.adel.features.users.api

import com.adel.features.users.data.UpdateProfileParams
import com.adel.features.users.domain.UserRole
import com.adel.features.users.service.UpdateProfileResult
import com.adel.features.users.service.UserService
import com.adel.plugins.JWT_ADMIN_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val role: String? = null,
)

fun Route.userRoutes(service: UserService) {
    route("/users") {
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user id")

            val user = service.getUser(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

            call.respond(user.toPublicDto())
        }

        authenticate(JWT_ADMIN_AUTH_NAME) {
            get {
                val users = service.getAllUsers()
                call.respond(users.map { it.toDto() })
            }

            patch("{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid user id")
                val request = call.receive<UpdateUserRequest>()
                // Validate and parse the role parameter if provided
                val roleEnum = try {
                    request.role?.let { UserRole.fromString(it) }
                } catch (e: IllegalArgumentException) {
                    return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid role: must be 'user' or 'admin'")
                    )
                }
                val params = UpdateProfileParams(
                    displayName = request.displayName,
                    hasDisplayName = request.displayName != null,
                    avatarUrl = request.avatarUrl,
                    hasAvatarUrl = request.avatarUrl != null,
                    bio = if (request.bio?.isEmpty() == true) null else request.bio,
                    hasBio = request.bio != null,
                    role = roleEnum,
                    hasRole = request.role != null
                )
                when (val result = service.updateProfile(id, params)) {
                    is UpdateProfileResult.Success -> call.respond(result.user.toDto())
                    UpdateProfileResult.UserNotFound -> call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                    UpdateProfileResult.DisplayNameTooShort -> call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Display name must be at least 2 characters")
                    )
                    UpdateProfileResult.DisplayNameTooLong -> call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Display name must be at most 50 characters")
                    )
                    UpdateProfileResult.AvatarUrlBlank -> call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Avatar URL cannot be blank")
                    )
                    UpdateProfileResult.BioTooLong -> call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Bio must be at most 100 characters")
                    )
                }
            }
        }
    }
}