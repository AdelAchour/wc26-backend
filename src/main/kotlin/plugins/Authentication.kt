package com.adel.plugins

import com.adel.common.security.JwtService
import com.adel.config.JwtConfig
import com.adel.features.users.domain.UserRole
import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

const val JWT_AUTH_NAME = "jwt-auth"
const val JWT_ADMIN_AUTH_NAME = "jwt-admin"

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

        // Admin auth provider — same JWT verification as above, but additionally
        // requires the role claim to equal "admin". Non-admins get 403 forbidden.
        jwt(JWT_ADMIN_AUTH_NAME) {
            realm = jwtConfig.realm

            verifier(
                JWT.require(jwtService.algorithm())
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )

            validate { credential ->
                val subject = credential.payload.subject
                val role = credential.payload.getClaim("role")?.asString()

                if (subject?.toLongOrNull() != null && role == UserRole.ADMIN.value) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                // Note: this fires for both "no token / bad token" (should be 401)
                // and "valid token but not admin" (should be 403).
                // Distinguishing requires inspecting the request — we keep it simple
                // and return 403 since the route is admin-scoped and the most likely
                // failure for a real client is "you're authenticated but not authorized".
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Admin access required")
                )
            }
        }
    }
}