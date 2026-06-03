package com.adel.features.users.data

import com.adel.common.database.dbQuery
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: Long): User? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun create(
        email: String,
        username: String,
        passwordHash: String,
        displayName: String,
    ): User = dbQuery {
        val id = UserTable.insert {
            it[UserTable.email] = email
            it[UserTable.username] = username
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.displayName] = displayName
            // role, created_at, updated_at use DB defaults
        } get UserTable.id

        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .map { it.toUser() }
            .single()
    }

    override suspend fun emailExists(email: String): Boolean = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .limit(1)
            .empty()
            .not()
    }

    override suspend fun usernameExists(username: String): Boolean = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.username eq username }
            .limit(1)
            .empty()
            .not()
    }

    override suspend fun updateAvatar(id: Long, avatarUrl: String?): User? = dbQuery {
        val updatedRows = UserTable.update({ UserTable.id eq id }) {
            it[UserTable.avatarUrl] = avatarUrl
            it[UserTable.updatedAt] = OffsetDateTime.now()
        }
        if (updatedRows > 0) findByIdInternal(id) else null
    }

    private fun findByIdInternal(id: Long): User? =
        UserTable
        .selectAll()
        .where { UserTable.id eq id }
        .map { it.toUser() }
        .singleOrNull()

    private fun ResultRow.toUser(): User = User(
        id = this[UserTable.id],
        email = this[UserTable.email],
        username = this[UserTable.username],
        passwordHash = this[UserTable.passwordHash],
        displayName = this[UserTable.displayName],
        avatarUrl = this[UserTable.avatarUrl],
        role = UserRole.fromString(this[UserTable.role]),
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt],
    )
}