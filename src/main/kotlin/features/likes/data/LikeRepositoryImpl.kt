package com.adel.features.likes.data

import com.adel.common.database.dbQuery
import com.adel.common.pagination.Cursor
import com.adel.common.pagination.CursorPage
import com.adel.features.posts.data.PostTable
import com.adel.features.posts.domain.Post
import com.adel.features.posts.domain.PostWithAuthor
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

class LikeRepositoryImpl : LikeRepository {

    override suspend fun exists(userId: Long, postId: Long): Boolean = dbQuery {
        LikeTable
            .selectAll()
            .where { (LikeTable.userId eq userId) and (LikeTable.postId eq postId) }
            .limit(1)
            .empty()
            .not()
    }

    override suspend fun create(userId: Long, postId: Long): Boolean = dbQuery {
        // insertIgnore returns null if the row already existed (PK conflict).
        // This is the idempotency primitive: "insert if not present, no-op if present."
        val result = LikeTable.insertIgnore {
            it[LikeTable.userId] = userId
            it[LikeTable.postId] = postId
        }
        result.insertedCount > 0
    }

    override suspend fun delete(userId: Long, postId: Long): Boolean = dbQuery {
        val rowsDeleted = LikeTable.deleteWhere {
            (LikeTable.userId eq userId) and (LikeTable.postId eq postId)
        }
        rowsDeleted > 0
    }

    override suspend fun findUsersWhoLikedPost(
        postId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<User> = dbQuery {
        val rows = (LikeTable innerJoin UserTable)
            .selectAll()
            .where { LikeTable.postId eq postId }
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (LikeTable.createdAt less c.createdAt) or
                                ((LikeTable.createdAt eq c.createdAt) and (LikeTable.userId less c.id))
                    }
                }
            }
            .orderBy(LikeTable.createdAt to SortOrder.DESC, LikeTable.userId to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toUser() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            // For this query, the cursor's "id" is the user_id (the second PK column).
            // We pair it with the like's createdAt to disambiguate ties.
            val lastLikeRow = (LikeTable innerJoin UserTable)
                .selectAll()
                .where { (LikeTable.postId eq postId) and (LikeTable.userId eq items.last().id) }
                .single()
            Cursor(lastLikeRow[LikeTable.createdAt], items.last().id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

    override suspend fun findPostsLikedByUser(
        userId: Long,
        cursor: Cursor?,
        limit: Int,
    ): CursorPage<PostWithAuthor> = dbQuery {
        // Three-way join: likes -> posts -> users (post authors)
        val rows = LikeTable
            .innerJoin(PostTable, { LikeTable.postId }, { PostTable.id })
            .innerJoin(UserTable, { PostTable.userId }, { UserTable.id })
            .selectAll()
            .where { LikeTable.userId eq userId }
            .apply {
                cursor?.let { c ->
                    andWhere {
                        (LikeTable.createdAt less c.createdAt) or
                                ((LikeTable.createdAt eq c.createdAt) and (LikeTable.postId less c.id))
                    }
                }
            }
            .orderBy(LikeTable.createdAt to SortOrder.DESC, LikeTable.postId to SortOrder.DESC)
            .limit(limit + 1)
            .map { it.toPostWithAuthor() }

        val hasMore = rows.size > limit
        val items = if (hasMore) rows.take(limit) else rows
        val nextCursor = if (hasMore) {
            // Last item's post id and like's createdAt
            val last = items.last().post
            val likeRow = LikeTable
                .selectAll()
                .where { (LikeTable.userId eq userId) and (LikeTable.postId eq last.id) }
                .single()
            Cursor(likeRow[LikeTable.createdAt], last.id).encode()
        } else null

        CursorPage(items, nextCursor)
    }

    override suspend fun areLikedBy(userId: Long, postIds: List<Long>): Set<Long> {
        if (postIds.isEmpty()) return emptySet()
        return dbQuery {
            LikeTable
                .select(LikeTable.postId)
                .where { (LikeTable.userId eq userId) and (LikeTable.postId inList postIds) }
                .map { it[LikeTable.postId] }
                .toSet()
        }
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
            bio = this[UserTable.bio],
            role = UserRole.fromString(this[UserTable.role]),
            createdAt = this[UserTable.createdAt],
            updatedAt = this[UserTable.updatedAt],
        ),
    )
}