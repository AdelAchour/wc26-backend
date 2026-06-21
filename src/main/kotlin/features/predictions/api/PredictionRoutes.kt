package com.adel.features.predictions.api

import com.adel.common.security.requireUserId
import com.adel.features.predictions.service.PredictionService
import com.adel.features.predictions.service.UpsertPredictionResult
import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
