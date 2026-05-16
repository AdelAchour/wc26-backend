package com.adel.features.posts.api

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPageDto
import com.adel.common.security.requireUserId
import com.adel.common.security.userIdOrNull
import com.adel.features.likes.service.LikeService
import com.adel.features.posts.domain.PostWithAuthor
import com.adel.features.posts.service.CreatePostResult
import com.adel.features.posts.service.DeletePostResult
import com.adel.features.posts.service.PostService
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(
    service: PostService,
    likeService: LikeService,
) {

    // -------- Public read endpoints with OPTIONAL auth --------
    authenticate(JWT_AUTH_NAME, optional = true) {

        route("/matches/{matchId}/posts") {
            get {
                val matchId = call.parameters["matchId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                val viewerId = call.userIdOrNull()

                val result = service.listPostsForMatch(matchId, cursor, limit)
                call.respond(buildPageDto(result.items, result.nextCursor, viewerId, likeService))
            }
        }

        route("/posts") {
            get {
                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                val viewerId = call.userIdOrNull()

                val result = service.listAllPosts(cursor, limit)
                call.respond(buildPageDto(result.items, result.nextCursor, viewerId, likeService))
            }
        }

        route("/posts/{id}") {
            get {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

                val viewerId = call.userIdOrNull()
                val post = service.getPost(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))

                val likedByCurrentUser = viewerId != null &&
                        likeService.whichArePostsLikedBy(viewerId, listOf(post.post.id)).isNotEmpty()

                call.respond(post.toDto(likedByCurrentUser))
            }
        }
    }

    // -------- Write endpoints (required auth) --------
    authenticate(JWT_AUTH_NAME) {

        post("/matches/{matchId}/posts") {
            val matchId = call.parameters["matchId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

            val userId = call.requireUserId()
            val request = call.receive<CreatePostRequest>()

            when (val result = service.createPost(userId, matchId, request.content)) {
                is CreatePostResult.Success -> {
                    val withAuthor = service.getPost(result.post.id)
                    if (withAuthor != null) {
                        call.respond(HttpStatusCode.Created, withAuthor.toDto(likedByCurrentUser = false))
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

/**
 * Builds a paginated DTO response, enriching each post with likedByCurrentUser
 * based on the viewer's actual likes. Uses a single batched query to avoid N+1.
 */
private suspend fun buildPageDto(
    items: List<PostWithAuthor>,
    nextCursor: String?,
    viewerId: Long?,
    likeService: LikeService,
): CursorPageDto<PostDto> {
    val likedSet: Set<Long> = if (viewerId != null) {
        likeService.whichArePostsLikedBy(viewerId, items.map { it.post.id })
    } else emptySet()

    val dtoItems = items.map { it.toDto(likedByCurrentUser = it.post.id in likedSet) }
    return CursorPageDto(items = dtoItems, nextCursor = nextCursor)
}