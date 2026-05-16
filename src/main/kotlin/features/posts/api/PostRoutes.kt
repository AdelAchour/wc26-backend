package com.adel.features.posts.api

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.toDto
import com.adel.features.posts.service.PostService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(service: PostService) {

    route("/matches/{matchId}/posts") {
        get {
            val matchId = call.parameters["matchId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

            val cursor = call.request.queryParameters["cursor"]
                ?.let { Cursor.decode(it) }

            val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                ?.coerceIn(1, 100)
                ?: 20

            val result = service.listPostsForMatch(matchId, cursor, limit)
            call.respond(result.toDto { it.toDto() })
        }
    }

    route("/posts/{id}") {
        get {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

            val post = service.getPost(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))

            call.respond(post.toDto())
        }
    }
}