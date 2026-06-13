package com.adel.features.notifications.data

import com.adel.common.database.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.time.OffsetDateTime
import java.time.ZoneOffset

class UserPushTokenRepositoryImpl : UserPushTokenRepository {

    override suspend fun registerToken(userId: Long, token: String): Unit = dbQuery {
        val existing = UserPushTokenTable.selectAll()
            .where { UserPushTokenTable.token eq token }
            .singleOrNull()

        if (existing == null) {
            UserPushTokenTable.insert {
                it[UserPushTokenTable.userId] = userId
                it[UserPushTokenTable.token] = token
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            }
        } else {
            val existingUserId = existing[UserPushTokenTable.userId]
            if (existingUserId == userId) {
                UserPushTokenTable.update({ UserPushTokenTable.token eq token }) {
                    it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                }
            } else {
                UserPushTokenTable.update({ UserPushTokenTable.token eq token }) {
                    it[UserPushTokenTable.userId] = userId
                    it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                }
            }
        }
    }

    override suspend fun unregisterToken(userId: Long, token: String): Unit = dbQuery {
        UserPushTokenTable.deleteWhere {
            (UserPushTokenTable.userId eq userId) and (UserPushTokenTable.token eq token)
        }
    }

    override suspend fun getTokensForUser(userId: Long): List<String> = dbQuery {
        UserPushTokenTable.select(UserPushTokenTable.token)
            .where { UserPushTokenTable.userId eq userId }
            .map { it[UserPushTokenTable.token] }
    }

    override suspend fun deleteTokens(tokens: List<String>): Unit = dbQuery {
        if (tokens.isNotEmpty()) {
            UserPushTokenTable.deleteWhere {
                UserPushTokenTable.token inList tokens
            }
        }
    }
}
