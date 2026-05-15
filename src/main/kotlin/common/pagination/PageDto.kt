package com.adel.common.pagination

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Long,
)
