package com.adel.features.users.domain

enum class UserRole(val value: String) {
    USER("user"),
    ADMIN("admin");

    companion object {
        fun fromString(value: String): UserRole =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown user role: $value")
    }
}