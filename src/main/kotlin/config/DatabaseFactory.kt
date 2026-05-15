package com.adel.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {

    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(config: DatabaseConfig) {
        logger.info("Initializing database connection pool...")
        val dataSource = createHikariDataSource(config)

        logger.info("Running Flyway migrations...")
        runMigrations(dataSource)

        logger.info("Connecting Exposed to data source...")
        Database.connect(dataSource)

        logger.info("Database initialization complete.")
    }

    private fun createHikariDataSource(config: DatabaseConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            driverClassName = config.driverClassName
            maximumPoolSize = config.maximumPoolSize
            isAutoCommit = false                  // Exposed manages transactions
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hikariConfig)
    }

    private fun runMigrations(dataSource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        val result = flyway.migrate()
        logger.info("Flyway: applied ${result.migrationsExecuted} migration(s).")
    }
}