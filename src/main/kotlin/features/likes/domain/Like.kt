package com.adel.features.likes.domain

import java.time.OffsetDateTime

data class Like(
    val userId: Long,
    val postId: Long,
    val createdAt: OffsetDateTime,
)