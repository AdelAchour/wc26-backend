package com.adel.features.predictions.api

import com.adel.plugins.JWT_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Prediction endpoints.
 *
 * B1: route shape + auth wiring only — handlers return 501 until B2 implements
 * the service (lock + teams-confirmed validation, upsert, list).
 */
fun Route.predictionRoutes() {
    authenticate(JWT_AUTH_NAME) {
        // All of the current user's predictions, for hydrating the match list /
        // detail / predictions tab.
        get("/me/predictions") {
            call.respond(HttpStatusCode.NotImplemented, mapOf("error" to "Not implemented yet"))
        }

        // Create or update the current user's prediction for a match.
        // Editable until kickoff; the server is the source of truth for the lock.
        put("/predictions/{matchId}") {
            call.respond(HttpStatusCode.NotImplemented, mapOf("error" to "Not implemented yet"))
        }
    }
}
