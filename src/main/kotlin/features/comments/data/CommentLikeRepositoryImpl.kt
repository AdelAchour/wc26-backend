package com.adel.features.comments.data

import com.adel.common.database.dbQuery
import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.users.data.UserTable
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class CommentLikeRepositoryImpl : CommentLikeRepository {

    override suspend fun exists(userId: Long, commentId: Long): Boolean = dbQuery {
        CommentLikeTable
            .selectAll()
            .where { (CommentLikeTable.userId eq userId) and (CommentLikeTable.commentId eq commentId) }
            .limit(1)
            .empty()
            .not()
    }

    override suspend fun create(userId: Long, commentId: Long): Boolean = dbQuery {
        val result = CommentLikeTable.insertIgnore {
            it[CommentLikeTable.userId] = userId
            it[CommentLikeTable.commentId] = commentId
        }
        result.insertedCount > 0
    }

    override suspend fun delete(userId: Long, commentId: Long): Boolean = dbQuery {
        val rowsDeleted = CommentLikeTable.deleteWhere {
            (CommentLikeTable.userId eq userId) and (CommentLikeTable.commentId eq commentId)
        }
        rowsDeleted > 0
    }

    override suspend fun areLikedBy(userId: Long, commentIds: List<Long>): Set<Long> {
        if (commentIds.isEmpty()) return emptySet()
        return dbQuery {
            CommentLikeTable
                .select(CommentLikeTable.commentId)
                .where { (CommentLikeTable.userId eq userId) and (CommentLikeTable.commentId inList commentIds) }
                .map { it[CommentLikeTable.commentId] }
                .toSet()
        }
    }

    override suspend fun findUsersWhoLikedComment(
        commentId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User> = dbQuery {
        val rows = (CommentLikeTable innerJoin UserTable)
            .selectAll()
            .where { CommentLikeTable.commentId eq commentId }
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (CommentLikeTable.createdAt less c.createdAt) or
                                ((CommentLikeTable.createdAt eq c.createdAt) and (CommentLikeTable.userId less c.id))
                    }
                }
            }
            .orderBy(CommentLikeTable.createdAt to SortOrder.DESC, CommentLikeTable.userId to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toUser() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            val lastLikeRow = (CommentLikeTable innerJoin UserTable)
                .selectAll()
                .where { (CommentLikeTable.commentId eq commentId) and (CommentLikeTable.userId eq items.last().id) }
                .single()
            Cursor(lastLikeRow[CommentLikeTable.createdAt], items.last().id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

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