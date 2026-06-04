package com.adel.features.auth.api

import com.adel.common.security.requireUserId
import com.adel.features.auth.service.AuthService
import com.adel.features.auth.service.LoginResult
import com.adel.features.auth.service.RegisterResult
import com.adel.features.users.api.toDto
import com.adel.features.users.data.UpdateProfileParams
import com.adel.features.users.service.UpdateProfileResult
import com.adel.features.users.service.UserService
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    service: AuthService,
    userService: UserService,
) {
    route("/auth") {

        post("register") {
            val request = call.receive<RegisterRequest>()

            when (val result = service.register(
                email = request.email,
                username = request.username,
                password = request.password,
                displayName = request.displayName,
            )) {
                is RegisterResult.Success -> call.respond(
                    HttpStatusCode.Created,
                    AuthResponse(
                        token = result.token,
                        userId = result.user.id,
                        email = result.user.email,
                        username = result.user.username,
                        displayName = result.user.displayName,
                    )
                )
                RegisterResult.InvalidEmail ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid email format"))
                RegisterResult.InvalidUsername ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username must be 3-20 characters, start with a letter, contain only lowercase letters, digits, and underscores"))
                RegisterResult.PasswordTooShort ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 8 characters"))
                RegisterResult.DisplayNameTooShort ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Display name must be at least 2 characters"))
                RegisterResult.EmailAlreadyTaken ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already registered"))
                RegisterResult.UsernameAlreadyTaken ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username already taken"))
            }
        }

        post("login") {
            val request = call.receive<LoginRequest>()

            when (val result = service.login(
                email = request.email,
                password = request.password,
            )) {
                is LoginResult.Success -> call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(
                        token = result.token,
                        userId = result.user.id,
                        email = result.user.email,
                        username = result.user.username,
                        displayName = result.user.displayName,
                    )
                )
                LoginResult.InvalidCredentials ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }

        // ---- Protected routes below ----
        authenticate(JWT_AUTH_NAME) {
            get("me") {
                val userId = call.requireUserId()

                val user = userService.getUser(userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))

                call.respond(user.toDto())
            }

            patch("me") {
                val userId = call.requireUserId()
                val request = call.receive<UpdateProfileRequest>()

                val params = UpdateProfileParams(
                    displayName = request.displayName,
                    hasDisplayName = request.displayName != null,
                    avatarUrl = request.avatarUrl,
                    hasAvatarUrl = request.avatarUrl != null,
                    // Map empty string "" to null to support clearing the bio
                    bio = if (request.bio?.isEmpty() == true) null else request.bio,
                    hasBio = request.bio != null
                )

                when (val result = userService.updateProfile(userId, params)) {
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