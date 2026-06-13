package com.adel.features.notifications.service

import com.adel.common.database.dbQuery
import com.adel.features.comments.data.CommentTable
import com.adel.features.notifications.data.UserPushTokenRepository
import com.adel.features.posts.data.PostTable
import com.adel.features.users.data.UserTable
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.adel.features.notifications.domain.NotificationType
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory

class FcmService(
    private val pushTokenRepository: UserPushTokenRepository
) {
    private val logger = LoggerFactory.getLogger(FcmService::class.java)
    private val fcmScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendPushNotificationAsync(
        senderId: Long,
        receiverId: Long,
        type: NotificationType,
        postId: Long,
        commentId: Long?
    ) {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.info("[FCM] Firebase not initialized. Skipping push notification.")
            return
        }

        fcmScope.launch {
            try {
                // 1. Fetch the receiver tokens
                val tokens = pushTokenRepository.getTokensForUser(receiverId)
                if (tokens.isEmpty()) {
                    logger.info("[FCM] No registered push tokens for user $receiverId. Skipping.")
                    return@launch
                }

                // 2. Fetch the sender displayName
                val senderName = dbQuery {
                    UserTable.select(UserTable.displayName)
                        .where { UserTable.id eq senderId }
                        .map { it[UserTable.displayName] }
                        .singleOrNull()
                } ?: "Someone"

                // 3. Fetch the content and prepare Title/Body
                val (title, body) = when (type) {
                    NotificationType.LIKE_POST -> {
                        val postContent = dbQuery {
                            PostTable.select(PostTable.content)
                                .where { PostTable.id eq postId }
                                .map { it[PostTable.content] }
                                .singleOrNull()
                        } ?: ""
                        "$senderName liked your post" to postContent.takeSnippet()
                    }
                    NotificationType.REPLY_POST -> {
                        val commentContent = commentId?.let { cid ->
                            dbQuery {
                                CommentTable.select(CommentTable.content)
                                    .where { CommentTable.id eq cid }
                                    .map { it[CommentTable.content] }
                                    .singleOrNull()
                            }
                        } ?: ""
                        "$senderName replied to your post" to commentContent.takeSnippet()
                    }
                    NotificationType.LIKE_COMMENT -> {
                        val commentContent = commentId?.let { cid ->
                            dbQuery {
                                CommentTable.select(CommentTable.content)
                                    .where { CommentTable.id eq cid }
                                    .map { it[CommentTable.content] }
                                    .singleOrNull()
                            }
                        } ?: ""
                        "$senderName liked your comment" to commentContent.takeSnippet()
                    }
                }

                // 4. Construct and send the multicast message
                val message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build()
                    )
                    .putData("postId", postId.toString())
                    .build()

                logger.info("[FCM] Dispatching push notification to user $receiverId (tokens count: ${tokens.size})")
                val response = FirebaseMessaging.getInstance().sendEachForMulticastAsync(message).get()
                logger.info("[FCM] Multicast send complete. Success: ${response.successCount}, Failure: ${response.failureCount}")

                // 5. Clean up unregistered/invalid tokens
                if (response.failureCount > 0) {
                    val tokensToRemove = mutableListOf<String>()
                    response.responses.forEachIndexed { index, res ->
                        if (!res.isSuccessful) {
                            val errorCode = res.exception?.messagingErrorCode?.name
                            if (errorCode == "UNREGISTERED" || errorCode == "INVALID_ARGUMENT") {
                                tokensToRemove.add(tokens[index])
                            }
                        }
                    }
                    if (tokensToRemove.isNotEmpty()) {
                        logger.info("[FCM] Deleting ${tokensToRemove.size} expired/invalid tokens from DB.")
                        pushTokenRepository.deleteTokens(tokensToRemove)
                    }
                }
            } catch (e: Exception) {
                logger.error("[FCM] Failed to dispatch push notification", e)
            }
        }
    }

    private fun String.takeSnippet(limit: Int = 100): String {
        return if (length <= limit) this else take(limit) + "..."
    }
}
