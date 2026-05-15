package com.adel.plugins

import com.adel.config.DatabaseFactory
import com.adel.config.loadDatabaseConfig
import com.adel.features.matches.MatchTable
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val sanityLogger = LoggerFactory.getLogger("DatabaseSanity")

fun Application.configureDatabase() {
    val config = loadDatabaseConfig()
    DatabaseFactory.init(config)

    // Temporary sanity check
    transaction {
        val count = MatchTable.selectAll().count()
        sanityLogger.info("Sanity check: matches table has $count row(s).")
    }
}