package com.adel

import io.ktor.client.request.get
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

}
