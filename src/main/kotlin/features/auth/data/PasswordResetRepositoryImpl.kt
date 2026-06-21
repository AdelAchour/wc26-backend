package com.adel.features.auth.data

import com.adel.common.database.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PasswordResetRepositoryImpl : PasswordResetRepository {

    override suspend fun upsertCode(email: String, code: String, expiryMinutes: Int): Unit = dbQuery {
        // Remove any previous codes for this email so only the latest is valid
        PasswordResetTable.deleteWhere { PasswordResetTable.email eq email }

        PasswordResetTable.insert {
            it[PasswordResetTable.email] = email
            it[PasswordResetTable.code] = code
            it[PasswordResetTable.expiresAt] = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(expiryMinutes.toLong())
        }
        Unit
    }

    override suspend fun findValidCode(email: String, code: String): String? = dbQuery {
        PasswordResetTable
            .selectAll()
            .where {
                (PasswordResetTable.email eq email) and
                (PasswordResetTable.code eq code) and
                (PasswordResetTable.expiresAt greaterEq OffsetDateTime.now(ZoneOffset.UTC))
            }
            .map { it[PasswordResetTable.code] }
            .singleOrNull()
    }

    override suspend fun deleteByEmail(email: String) = dbQuery {
        PasswordResetTable.deleteWhere { PasswordResetTable.email eq email }
        Unit
    }
}
