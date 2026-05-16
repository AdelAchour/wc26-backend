package com.adel.common.pagination

import kotlinx.serialization.Serializable

@Serializable
data class CursorPageDto<T>(
    val items: List<T>,
    val nextCursor: String?,
)

fun <D, W> CursorPage<D>.toDto(mapper: (D) -> W): CursorPageDto<W> = CursorPageDto(
    items = items.map(mapper),
    nextCursor = nextCursor,
)