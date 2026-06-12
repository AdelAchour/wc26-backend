package com.adel.features.comments.api

import com.adel.common.pagination.Cursor
import com.adel.common.pagination.toDto
import com.adel.common.security.requireUserId
import com.adel.common.security.userIdOrNull
import com.adel.features.comments.service.CommentService
import com.adel.features.comments.service.CommentLikeService
import com.adel.features.comments.service.CommentLikeResult
import com.adel.features.comments.service.CreateCommentResult
import com.adel.features.comments.service.DeleteCommentResult
import com.adel.features.users.api.toPublicDto
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.commentRoutes(
    service: CommentService,
    likeService: CommentLikeService
) {

    // -------- Public read endpoints with OPTIONAL auth --------
    authenticate(JWT_AUTH_NAME, optional = true) {
        route("/posts/{postId}/comments") {
            get {
                val postId = call.parameters["postId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull()
                    ?.coerceAtLeast(0)
                    ?: 0L

                val viewerId = call.userIdOrNull()
                val result = service.listForPost(postId, limit, offset)
                // Batch check which comments the current user has liked
                val likedByViewer = if (viewerId != null && result.items.isNotEmpty()) {
                    likeService.whichAreCommentsLikedBy(userId = viewerId, commentIds = result.items.map { it.comment.id })
                } else emptySet()

                call.respond(
                    result.toDto { it.toDto(likedByCurrentUser = it.comment.id in likedByViewer) }
                )
            }
        }

        // Who liked this comment?
        route("/comments/{commentId}/likes") {
            get {
                val commentId = call.parameters["commentId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid comment id"))

                val cursor = call.request.queryParameters["cursor"]?.let { Cursor.decode(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 100)
                    ?: 20

                val result = likeService.listLikersForComment(commentId, cursor, limit)
                call.respond(result.toDto { it.toPublicDto() })
            }
        }
    }

    // -------- Write endpoints (required auth) --------
    authenticate(JWT_AUTH_NAME) {
        // Create a comment
        post("/posts/{postId}/comments") {
            val postId = call.parameters["postId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid post id"))

            val userId = call.requireUserId()
            val request = call.receive<CreateCommentRequest>()

            when (val result = service.createComment(userId, postId, request.content)) {
                is CreateCommentResult.Success -> {
                    val withAuthor = service.getComment(result.comment.id)
                    if (withAuthor != null) {
                        call.respond(HttpStatusCode.Created, withAuthor.toDto())
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Comment created but could not be fetched"))
                    }
                }
                CreateCommentResult.ContentEmpty ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Content cannot be empty"))
                CreateCommentResult.ContentTooLong ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Content must be 300 characters or less"))
                CreateCommentResult.PostNotFound ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            }
        }

        // Delete a comment
        delete("/comments/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid comment id"))

            val userId = call.requireUserId()

            when (service.deleteComment(id, userId)) {
                DeleteCommentResult.Success -> call.respond(HttpStatusCode.NoContent)
                DeleteCommentResult.NotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Comment not found"))
                DeleteCommentResult.Forbidden -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You can only delete your own comments"))
            }
        }

        // Like a comment
        post("/comments/{commentId}/like") {
            val commentId = call.parameters["commentId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid comment id"))
            val userId = call.requireUserId()
            when (likeService.likeComment(userId, commentId)) {
                CommentLikeResult.Success -> call.respond(HttpStatusCode.NoContent)
                CommentLikeResult.CommentNotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Comment not found"))
            }
        }

        // Unlike a comment
        delete("/comments/{commentId}/like") {
            val commentId = call.parameters["commentId"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid comment id"))
            val userId = call.requireUserId()
            when (likeService.unlikeComment(userId, commentId)) {
                CommentLikeResult.Success -> call.respond(HttpStatusCode.NoContent)
                CommentLikeResult.CommentNotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Comment not found"))
            }
        }
    }
}