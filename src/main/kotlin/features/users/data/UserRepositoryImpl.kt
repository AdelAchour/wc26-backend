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

    override suspend fun updateProfile(id: Long, params: UpdateProfileParams): User? = dbQuery {
        val updatedRows = UserTable.update({ UserTable.id eq id }) {
            if (params.hasDisplayName && params.displayName != null) {
                it[UserTable.displayName] = params.displayName
            }
            if (params.hasAvatarUrl) {
                it[UserTable.avatarUrl] = params.avatarUrl
            }
            if (params.hasBio) {
                it[UserTable.bio] = params.bio
            }
            if (params.hasRole && params.role != null) {
                it[UserTable.role] = params.role.value
            }
            it[UserTable.updatedAt] = OffsetDateTime.now()
        }
        if (updatedRows > 0) {
            findByIdInternal(id)
        } else {
            null
        }
    }

    override suspend fun updatePasswordHash(email: String, newPasswordHash: String): Boolean = dbQuery {
        val updatedRows = UserTable.update({ UserTable.email eq email }) {
            it[UserTable.passwordHash] = newPasswordHash
            it[UserTable.updatedAt] = OffsetDateTime.now()
        }
        updatedRows > 0
    }

    override suspend fun findAll(): List<User> = dbQuery {
        UserTable
            .selectAll()
            .map { it.toUser() }
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
        bio = this[UserTable.bio],
        role = UserRole.fromString(this[UserTable.role]),
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt],
    )
}