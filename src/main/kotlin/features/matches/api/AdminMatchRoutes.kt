package com.adel.features.matches.api

import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.MatchStatus
import com.adel.features.matches.service.MatchService
import com.adel.features.matches.service.UpdateMatchResult
import com.adel.plugins.JWT_ADMIN_AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMatchRequest(
    val homeScore: Short? = null,
    val awayScore: Short? = null,
    val status: String? = null,
    val homeTeam: String? = null,
    val awayTeam: String? = null,
)

fun Route.adminMatchRoutes(
    service: MatchService,
    // Invoked after a match is finalized (FINISHED + both scores), so the
    // predictions feature can grade its predictions. Defaults to a no-op to
    // keep the matches feature decoupled from predictions.
    onMatchFinished: suspend (Match) -> Unit = {},
) {
    authenticate(JWT_ADMIN_AUTH_NAME) {
        patch("/admin/matches/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid match id"))

            val request = call.receive<UpdateMatchRequest>()

            val status = request.status?.let {
                runCatching { MatchStatus.fromString(it) }.getOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid status. Must be one of: scheduled, live, finished")
                    )
            }

            when (val result = service.updateMatchAdmin(
                id = id,
                homeScore = request.homeScore,
                awayScore = request.awayScore,
                status = status,
                homeTeam = request.homeTeam,
                awayTeam = request.awayTeam,
            )) {
                is UpdateMatchResult.Success -> {
                    val match = result.match
                    if (match.status == MatchStatus.FINISHED && match.homeScore != null && match.awayScore != null) {
                        onMatchFinished(match)
                    }
                    call.respond(match.toDto())
                }
                UpdateMatchResult.NotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Match not found"))
                UpdateMatchResult.InvalidScores -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Both scores must be provided together and must be non-negative"))
                UpdateMatchResult.NothingToUpdate -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "At least one field must be provided"))
                UpdateMatchResult.CannotUpdateGroupTeams -> call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Team names can only be changed for knockout stage matches")
                )
                UpdateMatchResult.InvalidTeam -> call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid team name. Must match one of the 48 participating countries.")
                )
            }
        }
    }
}