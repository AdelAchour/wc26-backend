package com.adel.features.comments.data

import com.adel.common.database.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
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
}