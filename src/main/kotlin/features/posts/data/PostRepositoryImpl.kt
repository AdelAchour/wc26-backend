package com.adel.features.posts.data

import com.adel.common.database.dbQuery
import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.posts.domain.Post
import com.adel.features.posts.domain.PostWithAuthor
import com.adel.features.users.data.UserTable
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class PostRepositoryImpl : PostRepository {

    override suspend fun findByIdWithAuthor(id: Long): PostWithAuthor? = dbQuery {
        (PostTable innerJoin UserTable)
            .selectAll()
            .where { PostTable.id eq id }
            .map { it.toPostWithAuthor() }
            .singleOrNull()
    }

    override suspend fun findByMatchWithAuthor(
        matchId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> = dbQuery {
        // Fetch limit + 1 rows so we know whether more pages exist
        val rows = (PostTable innerJoin UserTable)
            .selectAll()
            .where { PostTable.matchId eq matchId }
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (PostTable.createdAt less c.createdAt) or
                                ((PostTable.createdAt eq c.createdAt) and (PostTable.id less c.id))
                    }
                }
            }
            .orderBy(PostTable.createdAt to SortOrder.DESC, PostTable.id to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toPostWithAuthor() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            val last = items.last().post
            Cursor(last.createdAt, last.id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

    override suspend fun findAllWithAuthor(
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> = dbQuery {
        val rows = (PostTable innerJoin UserTable)
            .selectAll()
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (PostTable.createdAt less c.createdAt) or
                                ((PostTable.createdAt eq c.createdAt) and (PostTable.id less c.id))
                    }
                }
            }
            .orderBy(PostTable.createdAt to SortOrder.DESC, PostTable.id to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toPostWithAuthor() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            val last = items.last().post
            Cursor(last.createdAt, last.id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

    override suspend fun findByUserWithAuthor(
        userId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> = dbQuery {
        val rows = (PostTable innerJoin UserTable)
            .selectAll()
            .where { PostTable.userId eq userId }
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (PostTable.createdAt less c.createdAt) or
                                ((PostTable.createdAt eq c.createdAt) and (PostTable.id less c.id))
                    }
                }
            }
            .orderBy(PostTable.createdAt to SortOrder.DESC, PostTable.id to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toPostWithAuthor() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            val last = items.last().post
            Cursor(last.createdAt, last.id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

    override suspend fun create(userId: Long, matchId: Long, content: String): Post = dbQuery {
        val id = PostTable.insert {
            it[PostTable.userId] = userId
            it[PostTable.matchId] = matchId
            it[PostTable.content] = content
        } get PostTable.id

        PostTable
            .selectAll()
            .where { PostTable.id eq id }
            .map { it.toPost() }
            .single()
    }

    override suspend fun findById(id: Long): Post? = dbQuery {
        PostTable
            .selectAll()
            .where { PostTable.id eq id }
            .map { it.toPost() }
            .singleOrNull()
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        val rowsDeleted = PostTable.deleteWhere { PostTable.id eq id }
        rowsDeleted > 0
    }

    private fun ResultRow.toPost(): Post = Post(
        id = this[PostTable.id],
        userId = this[PostTable.userId],
        matchId = this[PostTable.matchId],
        content = this[PostTable.content],
        likeCount = this[PostTable.likeCount],
        commentCount = this[PostTable.commentCount],
        createdAt = this[PostTable.createdAt],
        updatedAt = this[PostTable.updatedAt],
    )

    private fun ResultRow.toPostWithAuthor(): PostWithAuthor = PostWithAuthor(
        post = Post(
            id = this[PostTable.id],
            userId = this[PostTable.userId],
            matchId = this[PostTable.matchId],
            content = this[PostTable.content],
            likeCount = this[PostTable.likeCount],
            commentCount = this[PostTable.commentCount],
            createdAt = this[PostTable.createdAt],
            updatedAt = this[PostTable.updatedAt],
        ),
        author = User(
            id = this[UserTable.id],
            email = this[UserTable.email],
            username = this[UserTable.username],
            passwordHash = this[UserTable.passwordHash],
            displayName = this[UserTable.displayName],
            avatarUrl = this[UserTable.avatarUrl],
            role = UserRole.fromString(this[UserTable.role]),
            createdAt = this[UserTable.createdAt],
            updatedAt = this[UserTable.updatedAt],
        ),
    )

    override suspend fun incrementLikeCount(postId: Long): Boolean = dbQuery {
        val updated = PostTable.update({ PostTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it.update(PostTable.likeCount, PostTable.likeCount + 1)
            }
        }
        updated > 0
    }

    override suspend fun decrementLikeCount(postId: Long): Boolean = dbQuery {
        // CHECK constraint on the table prevents going negative, so this is safe.
        val updated = PostTable.update({ PostTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it.update(PostTable.likeCount, PostTable.likeCount - 1)
            }
        }
        updated > 0
    }

    override suspend fun incrementCommentCount(postId: Long): Boolean = dbQuery {
        val updated = PostTable.update({ PostTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it.update(PostTable.commentCount, PostTable.commentCount + 1)
            }
        }
        updated > 0
    }

    override suspend fun decrementCommentCount(postId: Long): Boolean = dbQuery {
        val updated = PostTable.update({ PostTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it.update(PostTable.commentCount, PostTable.commentCount - 1)
            }
        }
        updated > 0
    }
}