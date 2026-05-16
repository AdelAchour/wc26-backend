package com.adel.common.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/**
 * Extracts the authenticated user's id from the JWT principal.
 *
 * Returns the user id, or null if no valid principal is attached
 * (e.g. the route is in an `authenticate(optional = true)` block
 * and the request came in unauthenticated, OR the JWT subject is malformed).
 *
 * Use [requireUserId] inside `authenticate { }` blocks where a principal
 * is guaranteed to be present.
 */
fun ApplicationCall.userIdOrNull(): Long? {
    val principal = principal<JWTPrincipal>() ?: return null
    return principal.payload.subject?.toLongOrNull()
}

/**
 * Like [userIdOrNull] but throws if no valid user id is found.
 * Use only inside required-authentication blocks where a principal is
 * always present.
 */
fun ApplicationCall.requireUserId(): Long =
    userIdOrNull()
        ?: throw IllegalStateException(
            "requireUserId called without a valid JWT principal. " +
                    "Ensure this route is inside an authenticate { } block."
        )