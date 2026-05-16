package com.adel.common.security

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Wraps BCrypt for password hashing and verification.
 *
 * Cost factor 12 means each hash takes ~250ms on modern hardware,
 * making brute force attacks computationally expensive while still
 * being responsive enough for interactive login.
 */
class PasswordHasher(
    private val cost: Int = DEFAULT_COST,
) {
    /**
     * Hashes a plaintext password. The salt is generated automatically
     * by BCrypt and embedded in the resulting hash string.
     */
    fun hash(plaintext: String): String =
        BCrypt.withDefaults().hashToString(cost, plaintext.toCharArray())

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     * Returns true if they match, false otherwise.
     */
    fun verify(plaintext: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plaintext.toCharArray(), hash).verified

    companion object {
        const val DEFAULT_COST = 12
    }
}