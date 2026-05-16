package com.adel.common.pagination

/**
 * Cursor-paginated result: items plus an optional next cursor.
 * When nextCursor is null, the caller has reached the end.
 */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
)