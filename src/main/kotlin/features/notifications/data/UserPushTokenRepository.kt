package com.adel.features.notifications.data

interface UserPushTokenRepository {
    suspend fun registerToken(userId: Long, token: String)
    suspend fun unregisterToken(userId: Long, token: String)
    suspend fun getTokensForUser(userId: Long): List<String>
    suspend fun deleteTokens(tokens: List<String>)
}
