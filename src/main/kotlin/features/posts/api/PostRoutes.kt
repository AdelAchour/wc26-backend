package com.adel.features.posts.api

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.toDto
import com.adel.common.security.requireUserId
import com.adel.common.security.userIdOrNull
import com.adel.features.posts.service.CreatePostResult
import com.adel.features.posts.service.DeletePostResult
import com.adel.features.posts.service.PostService
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(service: PostService) {

    // -------- Public read endpoints with OPTIONAL auth --------
    // If a valid token is provided, we know the current user and can
    // populate likedByCurrentUser. If no token (or invalid), reads still
    // work but likedByCurrentUser will be false for every post.
    authenticate(JWT_AUTH_NAME, optional = true) {
        route("/posts") {
            get {
                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20

                @Suppress("UNUSED_VARIABLE")
                val currentUserId = call.userIdOrNull()

                val result = service.listAllPosts(cursor, limit)
                call.respond(result.toDto { it.toDto() })
            }
        }

        route("/matches/{matchId}/posts") {
            get {
                val matchId = call.parameters["matchId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20

                // userIdOrNull will be wired into likedByCurrentUser in Stage 3
                @Suppress("UNUSED_VARIABLE")
                val currentUserId = call.userIdOrNull()

                val result = service.listPostsForMatch(matchId, cursor, limit)
                call.respond(result.toDto { it.toDto() })
            }
        }

        route("/posts/{id}") {
            get {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

                @Suppress("UNUSED_VARIABLE")
                val currentUserId = call.userIdOrNull()

                val post = service.getPost(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))

                call.respond(post.toDto())
            }
        }
    }

    // -------- Write endpoints (REQUIRED auth) --------
    authenticate(JWT_AUTH_NAME) {
        post("/matches/{matchId}/posts") {
            val matchId = call.parameters["matchId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

            val userId = call.requireUserId()
            val request = call.receive<CreatePostRequest>()

            when (val result = service.createPost(userId, matchId, request.content)) {
                is CreatePostResult.Success -> {
                    // We need to fetch with author for the response DTO
                    val withAuthor = service.getPost(result.post.id)
                    if (withAuthor != null) {
                        call.respond(HttpStatusCode.Created, withAuthor.toDto())
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Post created but could not be fetched"))
                    }
                }
                CreatePostResult.ContentEmpty ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Content cannot be empty"))
                CreatePostResult.ContentTooLong ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Content must be 500 characters or less"))
                CreatePostResult.MatchNotFound ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Match not found"))
            }
        }

        delete("/posts/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

            val userId = call.requireUserId()

            when (service.deletePost(id, userId)) {
                DeletePostResult.Success -> call.respond(HttpStatusCode.NoContent)
                DeletePostResult.NotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
                DeletePostResult.Forbidden -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You can only delete your own posts"))
            }
        }
    }
}