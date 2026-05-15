package com.adel.common.pagination

data class PaginatedResult<T>(
    val items: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Long,
)