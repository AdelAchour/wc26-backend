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
import com.adel.features.matches.service.MatchService
import com.adel.features.matches.api.toDto
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(
    service: PostService,
    likeService: LikeService,
    matchService: MatchService,
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
                // Match specific feed: keep match null to save bandwidth and queries
                call.respond(buildPageDto(result.items, result.nextCursor, viewerId, likeService))
            }
        }

        route("/posts") {
            get {
                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                val viewerId = call.userIdOrNull()

                val result = service.listAllPosts(cursor, limit)
                // Mixed context: enrich posts with their matches
                call.respond(buildPageDto(result.items, result.nextCursor, viewerId, likeService, matchService))
            }
        }

        route("/users/{userId}/posts") {
            get {
                val userId = call.parameters["userId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                val viewerId = call.userIdOrNull()

                val result = service.listPostsByUser(userId, cursor, limit)
                // Mixed context: enrich posts with their matches
                call.respond(buildPageDto(result.items, result.nextCursor, viewerId, likeService, matchService))
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

                // Fetch single match details via matchService
                val matchDto = matchService.getMatch(post.post.matchId)?.toDto()

                call.respond(post.toDto(likedByCurrentUser, matchDto))
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
                        // Match specific: keep match null
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
 * Builds a paginated DTO response, enriching each post with likedByCurrentUser.
 * If matchService is passed, it batch fetches matches in a single query to avoid N+1.
 */
private suspend fun buildPageDto(
    items: List<PostWithAuthor>,
    nextCursor: String?,
    viewerId: Long?,
    likeService: LikeService,
    matchService: MatchService? = null,
): CursorPageDto<PostDto> {
    val likedSet: Set<Long> = if (viewerId != null) {
        likeService.whichArePostsLikedBy(viewerId, items.map { it.post.id })
    } else emptySet()

    // Batch-load match details for scrolling feeds to prevent N+1 queries
    val matchesMap = if (matchService != null && items.isNotEmpty()) {
        val matchIds = items.map { it.post.matchId }.distinct()
        matchService.getMatchesByIds(matchIds).mapValues { it.value.toDto() }
    } else emptyMap()

    val dtoItems = items.map {
        it.toDto(
            likedByCurrentUser = it.post.id in likedSet,
            match = matchesMap[it.post.matchId]
        )
    }
    return CursorPageDto(items = dtoItems, nextCursor = nextCursor)
}