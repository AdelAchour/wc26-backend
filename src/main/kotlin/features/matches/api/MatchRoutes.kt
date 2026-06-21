package com.adel.features.matches.api

import com.adel.common.pagination.toDto
import com.adel.common.security.userIdOrNull
import com.adel.features.matches.domain.MatchStatus
import com.adel.features.matches.service.MatchService
import com.adel.features.predictions.service.PredictionService
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Match read endpoints. Optional auth — when the caller is signed in, each
 * match carries that user's prediction (mirrors how posts carry likedByCurrentUser).
 */
fun Route.matchRoutes(
    service: MatchService,
    predictionService: PredictionService,
) {
    authenticate(JWT_AUTH_NAME, optional = true) {
        route("/matches") {
            get {
                val status = call.request.queryParameters["status"]?.let {
                    runCatching { MatchStatus.fromString(it) }.getOrNull()
                }
                val stage = call.request.queryParameters["stage"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?.coerceIn(1, 200)
                    ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull()
                    ?.coerceAtLeast(0)
                    ?: 0L

                val result = service.listMatches(status, stage, limit, offset)
                val viewerId = call.userIdOrNull()
                // Fetch predictions for exactly the matches on this page, keyed by id.
                val predictions = if (viewerId != null) {
                    predictionService.getUserPredictionsForMatches(viewerId, result.items.map { it.id })
                } else emptyMap()

                call.respond(result.toDto { match -> match.toDto(predictions[match.id]) })
            }

            get("{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid match id")

                val match = service.getMatch(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Match not found")

                val viewerId = call.userIdOrNull()
                val prediction = viewerId?.let { predictionService.getUserPrediction(it, id) }

                call.respond(match.toDto(prediction))
            }
        }
    }
}
