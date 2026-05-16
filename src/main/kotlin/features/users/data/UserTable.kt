package com.adel.features.users.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = text("email")              // CITEXT in DB; Exposed treats as text
    val passwordHash = text("password_hash")
    val displayName = varchar("display_name", 50)
    val avatarUrl = text("avatar_url").nullable()
    val role = varchar("role", 10)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}