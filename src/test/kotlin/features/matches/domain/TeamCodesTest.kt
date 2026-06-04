package com.adel.features.matches.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeamCodesTest {

    @Test
    fun testValidTeamCodes() {
        assertEquals("ca", TeamCodes.fromTeamName("Canada"))
        assertEquals("mx", TeamCodes.fromTeamName("Mexico"))
        assertEquals("us", TeamCodes.fromTeamName("United States"))
        assertEquals("ci", TeamCodes.fromTeamName("Côte d’Ivoire"))
    }

    @Test
    fun testPlaceholderTeams() {
        assertNull(TeamCodes.fromTeamName("1A"))
        assertNull(TeamCodes.fromTeamName("2B"))
        assertNull(TeamCodes.fromTeamName("W73"))
    }

    @Test
    fun testInvalidOrUnknownTeams() {
        assertNull(TeamCodes.fromTeamName(""))
        assertNull(TeamCodes.fromTeamName("Unknown Team"))
        assertNull(TeamCodes.fromTeamName("canada")) // case sensitivity check
    }
}
