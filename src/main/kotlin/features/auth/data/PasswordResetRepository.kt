package com.adel.features.auth.data

interface PasswordResetRepository {
    /** Deletes any existing codes for this email, then inserts a new one. */
    suspend fun upsertCode(email: String, code: String, expiryMinutes: Int)

    /** Returns the code if it exists and has not expired, null otherwise. */
    suspend fun findValidCode(email: String, code: String): String?

    /** Deletes all reset codes for the given email (used after successful reset). */
    suspend fun deleteByEmail(email: String)
}
