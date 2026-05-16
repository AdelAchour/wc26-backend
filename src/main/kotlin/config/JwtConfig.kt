package com.adel.config

import io.ktor.server.application.*

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationHours: Long,
)

fun Application.loadJwtConfig(): JwtConfig {
    val jwt = environment.config.config("jwt")
    return JwtConfig(
        secret = jwt.property("secret").getString(),
        issuer = jwt.property("issuer").getString(),
        audience = jwt.property("audience").getString(),
        realm = jwt.property("realm").getString(),
        expirationHours = jwt.property("expirationHours").getString().toLong(),
    )
}