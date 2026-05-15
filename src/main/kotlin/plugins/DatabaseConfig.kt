package com.adel.plugins

import com.adel.config.DatabaseFactory
import com.adel.config.loadDatabaseConfig
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

private val sanityLogger = LoggerFactory.getLogger("DatabaseSanity")

fun Application.configureDatabase() {
    val config = loadDatabaseConfig()
    DatabaseFactory.init(config)
}