package com.adel.features.matches.api

import com.adel.common.pagination.toDto
import com.adel.features.matches.domain.MatchStatus
import com.adel.features.matches.service.MatchService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.matchRoutes(service: MatchService) {
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
            call.respond(result.toDto { it.toDto() })
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid match id")

            val match = service.getMatch(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Match not found")

            call.respond(match.toDto())
        }
    }
}