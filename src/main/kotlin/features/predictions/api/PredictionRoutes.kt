package com.adel.features.predictions.api

import com.adel.common.pagination.toDto
import com.adel.common.security.requireUserId
import com.adel.common.security.userIdOrNull
import com.adel.features.matches.api.toDto as toMatchDto
import com.adel.features.predictions.service.PredictionService
import com.adel.features.predictions.service.UpsertPredictionResult
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Public leaderboard. Optional auth — when the caller is logged in, the
 * response also carries their own rank for the sticky "you" row.
 */
fun Route.leaderboardRoutes(service: PredictionService) {
    authenticate(JWT_AUTH_NAME, optional = true) {
        get("/leaderboard") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                ?.coerceIn(1, 100) ?: 20
            val offset = call.request.queryParameters["offset"]?.toLongOrNull()
                ?.coerceAtLeast(0) ?: 0L

            val page = service.getLeaderboard(limit, offset)
            val me = call.userIdOrNull()?.let { service.getMyRank(it) }

            call.respond(
                LeaderboardResponseDto(
                    entries = page.toDto { it.toDto() },
                    me = me?.toDto(),
                )
            )
        }
    }
}

/** Public per-user prediction stats for profile screens. */
fun Route.userPredictionStatsRoutes(service: PredictionService) {
    get("/users/{id}/prediction-stats") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))

        val stats = service.getUserStats(id)
            ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))

        call.respond(stats.toDto())
    }

    // A user's prediction history — finished matches only, each with their
    // prediction embedded (teams, actual score, points), most recent first.
    get("/users/{id}/predictions") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))

        val history = service.getUserPredictionHistory(id)
        call.respond(history.map { (match, prediction) -> match.toMatchDto(prediction) })
    }
}

fun Route.predictionRoutes(service: PredictionService) {
    authenticate(JWT_AUTH_NAME) {
        // All of the current user's predictions.
        get("/me/predictions") {
            val userId = call.requireUserId()
            call.respond(service.getMyPredictions(userId).map { it.toDto() })
        }

        // Create or update the current user's prediction for a match.
        // Editable until kickoff; the server is the source of truth for the lock.
        put("/predictions/{matchId}") {
            val matchId = call.parameters["matchId"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

            val userId = call.requireUserId()
            val request = call.receive<UpsertPredictionRequest>()

            when (val result = service.upsertPrediction(userId, matchId, request.homeScore, request.awayScore)) {
                is UpsertPredictionResult.Success ->
                    call.respond(HttpStatusCode.OK, result.prediction.toDto())
                UpsertPredictionResult.MatchNotFound ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Match not found"))
                UpsertPredictionResult.TeamsNotConfirmed ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Teams are not confirmed yet"))
                UpsertPredictionResult.Locked ->
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Predictions are locked for this match"))
                UpsertPredictionResult.InvalidScores ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Scores must be between 0 and 99"))
            }
        }
    }
}
