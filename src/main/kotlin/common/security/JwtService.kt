package com.adel.common.security

import com.adel.config.JwtConfig
import com.adel.features.users.domain.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * Creates and signs JWT tokens for authenticated users.
 *
 * Token contains:
 *   - sub: the user's id (as String)
 *   - email: the user's email
 *   - role: "user" or "admin"
 *   - iss: configured issuer
 *   - aud: configured audience
 *   - iat: issued-at timestamp
 *   - exp: expiration timestamp
 *
 * Signed with HS256 using the configured secret.
 */
class JwtService(private val config: JwtConfig) {

    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    fun generateToken(user: User): String {
        val now = System.currentTimeMillis()
        val expiresAt = now + config.expirationHours * 60 * 60 * 1000

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .withClaim("role", user.role.value)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    /** Exposes the algorithm so the Ktor JWT plugin can use it for verification. */
    fun algorithm(): Algorithm = algorithm
}