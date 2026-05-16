package com.adel.common.pagination

import java.time.OffsetDateTime
import java.util.Base64

/**
 * A pagination cursor representing a position in a list ordered by (createdAt DESC, id DESC).
 *
 * Encoded as base64("<createdAt ISO-8601>|<id>") so the client treats it as opaque.
 */
data class Cursor(
    val createdAt: OffsetDateTime,
    val id: Long,
) {
    fun encode(): String {
        val raw = "${createdAt}|$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }

    companion object {
        fun decode(encoded: String): Cursor? = runCatching {
            val raw = String(Base64.getUrlDecoder().decode(encoded))
            val (createdAtStr, idStr) = raw.split("|")
            Cursor(OffsetDateTime.parse(createdAtStr), idStr.toLong())
        }.getOrNull()
    }
}