package com.adel.config

import io.ktor.server.application.*

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val fromName: String,
    val startTls: Boolean,
)

fun Application.loadSmtpConfig(): SmtpConfig {
    val smtp = environment.config.config("smtp")
    return SmtpConfig(
        host = smtp.property("host").getString(),
        port = smtp.property("port").getString().toInt(),
        username = smtp.property("username").getString(),
        password = smtp.property("password").getString(),
        fromAddress = smtp.property("fromAddress").getString(),
        fromName = smtp.property("fromName").getString(),
        startTls = smtp.property("startTls").getString().toBoolean(),
    )
}
