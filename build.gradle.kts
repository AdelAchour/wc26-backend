plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

group = "com.adel"
version = "1.3"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs = listOf("-Duser.timezone=UTC")
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

// Generates a build-info.properties file containing the project version,
// so the running app can report its version without hardcoding it.
val generateBuildInfo by tasks.registering {
    val versionString = project.version.toString()
    val outputDir = layout.buildDirectory.dir("generated/build-info")
    inputs.property("version", versionString)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("build-info.properties").asFile
        file.parentFile.mkdirs()
        file.writeText("version=$versionString\n")
    }
}

// Include the generated file in the app's resources, and make sure
// the generation runs before resources are gathered.
sourceSets {
    main {
        resources {
            srcDir(layout.buildDirectory.dir("generated/build-info"))
        }
    }
}

tasks.named("processResources") {
    dependsOn(generateBuildInfo)
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
