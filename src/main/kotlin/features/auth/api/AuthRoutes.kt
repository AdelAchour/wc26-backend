package com.adel.features.auth.api

import com.adel.features.auth.service.AuthService
import com.adel.features.auth.service.LoginResult
import com.adel.features.auth.service.RegisterResult
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(service: AuthService) {
    route("/auth") {

        post("register") {
            val request = call.receive<RegisterRequest>()

            when (val result = service.register(
                email = request.email,
                password = request.password,
                displayName = request.displayName,
            )) {
                is RegisterResult.Success -> call.respond(
                    HttpStatusCode.Created,
                    AuthResponse(
                        token = result.token,
                        userId = result.user.id,
                        email = result.user.email,
                        displayName = result.user.displayName,
                    )
                )
                RegisterResult.InvalidEmail ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid email format"))
                RegisterResult.PasswordTooShort ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 8 characters"))
                RegisterResult.DisplayNameTooShort ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Display name must be at least 2 characters"))
                RegisterResult.EmailAlreadyTaken ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already registered"))
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
                        displayName = result.user.displayName,
                    )
                )
                LoginResult.InvalidCredentials ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }
    }
}