package com.adel.plugins

import com.adel.common.security.JwtService
import com.adel.config.JwtConfig
import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

const val JWT_AUTH_NAME = "jwt-auth"

fun Application.configureAuthentication(
    jwtConfig: JwtConfig,
    jwtService: JwtService,
) {
    install(Authentication) {
        jwt(JWT_AUTH_NAME) {
            realm = jwtConfig.realm

            verifier(
                JWT.require(jwtService.algorithm())
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )

            validate { credential ->
                val subject = credential.payload.subject
                if (subject?.toLongOrNull() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or missing authentication token")
                )
            }
        }
    }
}