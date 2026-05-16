package com.adel.features.users.data

import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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
        passwordHash: String,
        displayName: String,
    ): User = dbQuery {
        val id = UserTable.insert {
            it[UserTable.email] = email
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

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toUser(): User = User(
        id = this[UserTable.id],
        email = this[UserTable.email],
        passwordHash = this[UserTable.passwordHash],
        displayName = this[UserTable.displayName],
        avatarUrl = this[UserTable.avatarUrl],
        role = UserRole.fromString(this[UserTable.role]),
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt],
    )
}