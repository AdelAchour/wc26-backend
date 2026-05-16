package com.adel.features.users.api

import com.adel.features.users.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(service: UserService) {
    route("/users") {
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user id")

            val user = service.getUser(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

            call.respond(user.toDto())
        }
    }
}