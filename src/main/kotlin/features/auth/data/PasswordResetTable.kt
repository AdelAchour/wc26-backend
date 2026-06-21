package com.adel.features.auth.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object PasswordResetTable : Table("password_resets") {
    val id = long("id").autoIncrement()
    val email = text("email")
    val code = varchar("code", 6)
    val expiresAt = timestampWithTimeZone("expires_at")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
