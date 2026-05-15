package com.adel.config

import io.ktor.server.application.*

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
    val maximumPoolSize: Int,
)

fun Application.loadDatabaseConfig(): DatabaseConfig {
    val db = environment.config.config("database")
    return DatabaseConfig(
        jdbcUrl = db.property("jdbcUrl").getString(),
        username = db.property("username").getString(),
        password = db.property("password").getString(),
        driverClassName = db.property("driverClassName").getString(),
        maximumPoolSize = db.property("maximumPoolSize").getString().toInt(),
    )
}