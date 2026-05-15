package com.adel.plugins

import com.adel.config.DatabaseFactory
import com.adel.config.loadDatabaseConfig
import io.ktor.server.application.*

fun Application.configureDatabase() {
    val config = loadDatabaseConfig()
    DatabaseFactory.init(config)
}