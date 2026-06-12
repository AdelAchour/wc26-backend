package com.adel

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        // loads default configuration
        configure()
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `test matches endpoint returns team codes`() = testApplication {
        configure()
        val response = client.get("/matches")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("homeTeamCode"))
        assertTrue(body.contains("awayTeamCode"))
        // Match 1 is Mexico vs South Africa
        assertTrue(body.contains("\"homeTeam\":\"Mexico\""))
        assertTrue(body.contains("\"homeTeamCode\":\"mx\""))
        assertTrue(body.contains("\"awayTeam\":\"South Africa\""))
        assertTrue(body.contains("\"awayTeamCode\":\"za\""))
    }

    // system status
    @Test
    fun `test system status endpoint returns config`() = testApplication {
        configure()
        val response = client.get("/system-status")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("min_android_version"))
        assertTrue(body.contains("maintenance_mode"))
        assertTrue(body.contains("android_update_url"))
    }

    @Test
    fun `test interceptor blocks outdated version`() = testApplication {
        configure()
        val response = client.get("/matches") {
            header("X-App-Version", "0")
        }
        assertEquals(HttpStatusCode.UpgradeRequired, response.status) // HTTP 426
        val body = response.bodyAsText()
        assertTrue(body.contains("android_update_url"))
        assertTrue(body.contains("min_android_version"))
    }

    @Test
    fun `test interceptor allows current version`() = testApplication {
        configure()
        val response = client.get("/matches") {
            header("X-App-Version", "2") // 2 >= 1
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    private fun generateTestToken(userId: Long, role: String): String {
        val secret = "dev-secret-do-not-use-in-production-please-change-this-to-something-long-and-random"
        val algorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(secret)
        return com.auth0.jwt.JWT.create()
            .withIssuer("wc26-backend")
            .withAudience("wc26-clients")
            .withSubject(userId.toString())
            .withClaim("email", "test-$userId@example.com")
            .withClaim("role", role)
            .withIssuedAt(java.util.Date())
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600 * 1000))
            .sign(algorithm)
    }

    @Test
    fun `test get users unauthorized`() = testApplication {
        configure()
        val response = client.get("/users")
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `test get users forbidden for non-admin`() = testApplication {
        configure()
        val token = generateTestToken(123L, "user")
        val response = client.get("/users") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
