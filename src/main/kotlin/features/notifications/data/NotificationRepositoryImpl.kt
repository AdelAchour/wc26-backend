package com.adel.features.notifications.data

import com.adel.common.database.dbQuery
import com.adel.common.pagination.PaginatedResult
import com.adel.features.comments.data.CommentTable
import com.adel.features.notifications.domain.Notification
import com.adel.features.notifications.domain.NotificationCommentSnippet
import com.adel.features.notifications.domain.NotificationPostSnippet
import com.adel.features.notifications.domain.NotificationType
import com.adel.features.posts.data.PostTable
import com.adel.features.users.data.UserTable
import com.adel.features.users.domain.User
import com.adel.features.users.domain.UserRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.OffsetDateTime
import java.time.ZoneOffset

class NotificationRepositoryImpl : NotificationRepository {

    override suspend fun create(
        senderId: Long,
        receiverId: Long,
        type: NotificationType,
        postId: Long,
        commentId: Long?
    ): Boolean = dbQuery {
        val inserted = NotificationTable.insert {
            it[NotificationTable.senderId] = senderId
            it[NotificationTable.receiverId] = receiverId
            it[NotificationTable.type] = type.name
            it[NotificationTable.postId] = postId
            it[NotificationTable.commentId] = commentId
            it[NotificationTable.createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
        inserted.insertedCount > 0
    }

    override suspend fun findNotificationsForUser(
        userId: Long,
        limit: Int,
        offset: Long
    ): PaginatedResult<Notification> = dbQuery {
        val baseQuery = NotificationTable
            .innerJoin(UserTable, { NotificationTable.senderId }, { UserTable.id })
            .innerJoin(PostTable, { NotificationTable.postId }, { PostTable.id })
            .leftJoin(CommentTable, { NotificationTable.commentId }, { CommentTable.id })
            .select(
                NotificationTable.id,
                NotificationTable.receiverId,
                NotificationTable.type,
                NotificationTable.isRead,
                NotificationTable.createdAt,
                UserTable.id,
                UserTable.email,
                UserTable.username,
                UserTable.passwordHash,
                UserTable.displayName,
                UserTable.avatarUrl,
                UserTable.bio,
                UserTable.role,
                UserTable.createdAt,
                UserTable.updatedAt,
                PostTable.id,
                PostTable.content,
                CommentTable.id,
                CommentTable.content
            )
            .where { NotificationTable.receiverId eq userId }

        val totalCount = baseQuery.count()

        val items = baseQuery
            .orderBy(NotificationTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { row ->
                Notification(
                    id = row[NotificationTable.id],
                    receiverId = row[NotificationTable.receiverId],
                    sender = User(
                        id = row[UserTable.id],
                        email = row[UserTable.email],
                        username = row[UserTable.username],
                        passwordHash = row[UserTable.passwordHash],
                        displayName = row[UserTable.displayName],
                        avatarUrl = row[UserTable.avatarUrl],
                        bio = row[UserTable.bio],
                        role = UserRole.fromString(row[UserTable.role]),
                        createdAt = row[UserTable.createdAt],
                        updatedAt = row[UserTable.updatedAt]
                    ),
                    type = NotificationType.valueOf(row[NotificationTable.type]),
                    post = NotificationPostSnippet(
                        id = row[PostTable.id],
                        content = row[PostTable.content]
                    ),
                    comment = row.getOrNull(CommentTable.id)?.let { commentId ->
                        NotificationCommentSnippet(
                            id = commentId,
                            content = row.getOrNull(CommentTable.content) ?: ""
                        )
                    },
                    isRead = row[NotificationTable.isRead],
                    createdAt = row[NotificationTable.createdAt]
                )
            }

        PaginatedResult(
            items = items,
            total = totalCount,
            limit = limit,
            offset = offset
        )
    }

    override suspend fun countUnread(userId: Long): Long = dbQuery {
        NotificationTable
            .selectAll()
            .where { (NotificationTable.receiverId eq userId) and (NotificationTable.isRead eq false) }
            .count()
    }

    override suspend fun markAllAsRead(userId: Long): Boolean = dbQuery {
        val updated = NotificationTable.update({ NotificationTable.receiverId eq userId }) {
            it[isRead] = true
        }
        updated > 0
    }

    override suspend fun markAsRead(notificationId: Long, userId: Long): Boolean = dbQuery {
        val updated = NotificationTable.update({
            (NotificationTable.id eq notificationId) and (NotificationTable.receiverId eq userId)
        }) {
            it[isRead] = true
        }
        updated > 0
    }

    override suspend fun deleteLikeNotification(senderId: Long, postId: Long): Boolean = dbQuery {
        val deleted = NotificationTable.deleteWhere {
            (NotificationTable.senderId eq senderId) and
                    (NotificationTable.postId eq postId) and
                    (NotificationTable.type eq NotificationType.LIKE_POST.name)
        }
        deleted > 0
    }

    override suspend fun deleteCommentLikeNotification(senderId: Long, commentId: Long): Boolean = dbQuery {
        val deleted = NotificationTable.deleteWhere {
            (NotificationTable.senderId eq senderId) and
                    (NotificationTable.commentId eq commentId) and
                    (NotificationTable.type eq NotificationType.LIKE_COMMENT.name)
        }
        deleted > 0
    }
}