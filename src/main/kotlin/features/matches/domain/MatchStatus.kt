package com.adel.features.matches.domain

enum class MatchStatus(val value: String) {
    SCHEDULED("scheduled"),
    LIVE("live"),
    FINISHED("finished");

    companion object {
        fun fromString(value: String): MatchStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown match status: $value")
    }
}