package com.adel.features.likes.api

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPageDto
import com.adel.common.pagination.toDto
import com.adel.common.security.requireUserId
import com.adel.common.security.userIdOrNull
import com.adel.features.likes.service.LikeResult
import com.adel.features.likes.service.LikeService
import com.adel.features.posts.api.toDto
import com.adel.features.users.api.toPublicDto
import com.adel.features.matches.service.MatchService
import com.adel.features.matches.api.toDto
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.likeRoutes(
    service: LikeService,
    matchService: MatchService,
) {

    // -------- Public read endpoints with optional auth --------
    authenticate(JWT_AUTH_NAME, optional = true) {

        // Who liked this post?
        route("/posts/{postId}/likes") {
            get {
                val postId = call.parameters["postId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20

                val result = service.listLikersForPost(postId, cursor, limit)
                call.respond(result.toDto { it.toPublicDto() })
            }
        }

        // What posts has this user liked?
        route("/users/{userId}/likes") {
            get {
                val targetUserId = call.parameters["userId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

                val viewerId = call.userIdOrNull()
                val result = service.listPostsLikedBy(targetUserId, cursor, limit)

                // Determine which of these posts THE VIEWER has liked
                val likedByViewer: Set<Long> = if (viewerId != null) {
                    service.whichArePostsLikedBy(viewerId, result.items.map { it.post.id })
                } else emptySet()

                // Batch fetch match details via matchService to avoid N+1 queries
                val matchesMap = if (result.items.isNotEmpty()) {
                    val matchIds = result.items.map { it.post.matchId }.distinct()
                    matchService.getMatchesByIds(matchIds).mapValues { it.value.toDto() }
                } else emptyMap()

                val responseItems = result.items.map { postWithAuthor ->
                    postWithAuthor.toDto(
                        likedByCurrentUser = postWithAuthor.post.id in likedByViewer,
                        match = matchesMap[postWithAuthor.post.matchId]
                    )
                }

                call.respond(
                    CursorPageDto(
                        items = responseItems,
                        nextCursor = result.nextCursor,
                    )
                )
            }
        }
    }

    // -------- Write endpoints (required auth) --------
    authenticate(JWT_AUTH_NAME) {

        post("/posts/{postId}/like") {
            val postId = call.parameters["postId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

            val userId = call.requireUserId()

            when (service.likePost(userId, postId)) {
                LikeResult.Success -> call.respond(HttpStatusCode.NoContent)
                LikeResult.PostNotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            }
        }

        delete("/posts/{postId}/like") {
            val postId = call.parameters["postId"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

            val userId = call.requireUserId()

            when (service.unlikePost(userId, postId)) {
                LikeResult.Success -> call.respond(HttpStatusCode.NoContent)
                LikeResult.PostNotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            }
        }
    }
}