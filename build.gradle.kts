plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

group = "com.adel"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs = listOf("-Duser.timezone=UTC")
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.logback.classic)

    // Exposed — Kotlin SQL framework
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Postgres driver
    implementation(libs.postgresql)

    // HikariCP — connection pool
    implementation(libs.hikari.cp)

    // Flyway — DB migrations
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.bcrypt)

    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
