package com.adel.plugins

import com.adel.config.loadJwtConfig
import com.adel.features.auth.api.authRoutes
import com.adel.features.auth.di.AuthComponent
import com.adel.features.matches.api.matchRoutes
import com.adel.features.matches.di.MatchComponent
import com.adel.features.users.api.userRoutes
import com.adel.features.users.di.UserComponent
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val jwtConfig = loadJwtConfig()

    val matchComponent = MatchComponent()
    val userComponent = UserComponent()
    val authComponent = AuthComponent(userComponent.repository, jwtConfig)

    routing {
        get("/") {
            call.respondText("WC26 Backend is alive ⚽ — 🇨🇦🇲🇽🇺🇸")
        }

        matchRoutes(matchComponent.service)
        userRoutes(userComponent.service)
        authRoutes(authComponent.service)
    }
}