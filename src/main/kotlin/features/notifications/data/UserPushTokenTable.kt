package com.adel.features.notifications.data

import com.adel.features.users.data.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserPushTokenTable : Table("user_push_tokens") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UserTable.id)
    val token = varchar("token", 512).uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
