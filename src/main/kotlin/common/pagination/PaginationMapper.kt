package com.adel.common.pagination


/**
 * Map a domain PaginatedResult<D> into a wire-format PageDto<W>
 * by mapping each item with the provided mapper function.
 */
fun <D, W> PaginatedResult<D>.toDto(mapper: (D) -> W): PageDto<W> = PageDto(
    items = items.map(mapper),
    total = total,
    limit = limit,
    offset = offset,
)