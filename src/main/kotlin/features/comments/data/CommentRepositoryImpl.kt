package com.adel.features.comments.data

import com.adel.common.database.dbQuery
import com.adel.common.pagination.PaginatedResult
import com.adel.features.comments.domain.Comment
import com.adel.features.comments.domain.CommentWithAuthor
import com.adel.features.users.data.UserTable
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder

class CommentRepositoryImpl : CommentRepository {

    override suspend fun findById(id: Long): Comment? = dbQuery {
        CommentTable
            .selectAll()
            .where { CommentTable.id eq id }
            .map { it.toComment() }
            .singleOrNull()
    }

    override suspend fun findByIdWithAuthor(id: Long): CommentWithAuthor? = dbQuery {
        (CommentTable innerJoin UserTable)
            .selectAll()
            .where { CommentTable.id eq id }
            .map { it.toCommentWithAuthor() }
            .singleOrNull()
    }

    override suspend fun listByPost(
        postId: Long,
        limit: Int,
        offset: Long,
    ): PaginatedResult<CommentWithAuthor> = dbQuery {
        val items = (CommentTable innerJoin UserTable)
            .selectAll()
            .where { CommentTable.postId eq postId }
            .orderBy(CommentTable.createdAt to SortOrder.DESC, CommentTable.id to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toCommentWithAuthor() }

        val total = CommentTable
            .selectAll()
            .where { CommentTable.postId eq postId }
            .count()

        PaginatedResult(items, total, limit, offset)
    }

    override suspend fun create(userId: Long, postId: Long, content: String): Comment = dbQuery {
        val id = CommentTable.insert {
            it[CommentTable.userId] = userId
            it[CommentTable.postId] = postId
            it[CommentTable.content] = content
        } get CommentTable.id

        CommentTable
            .selectAll()
            .where { CommentTable.id eq id }
            .map { it.toComment() }
            .single()
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        val rowsDeleted = CommentTable.deleteWhere { CommentTable.id eq id }
        rowsDeleted > 0
    }

    override suspend fun incrementLikeCount(commentId: Long): Boolean = dbQuery {
        val updated = CommentTable.update({ CommentTable.id eq commentId }) {
            with(SqlExpressionBuilder) {
                it.update(CommentTable.likeCount, CommentTable.likeCount + 1)
            }
        }
        updated > 0
    }
    override suspend fun decrementLikeCount(commentId: Long): Boolean = dbQuery {
        val updated = CommentTable.update({ CommentTable.id eq commentId }) {
            with(SqlExpressionBuilder) {
                it.update(CommentTable.likeCount, CommentTable.likeCount - 1)
            }
        }
        updated > 0
    }

    private fun ResultRow.toComment(): Comment = Comment(
        id = this[CommentTable.id],
        userId = this[CommentTable.userId],
        postId = this[CommentTable.postId],
        content = this[CommentTable.content],
        likeCount = this[CommentTable.likeCount],
        createdAt = this[CommentTable.createdAt],
    )

    private fun ResultRow.toCommentWithAuthor(): CommentWithAuthor = CommentWithAuthor(
        comment = toComment(),
        author = User(
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
        ),
    )
}