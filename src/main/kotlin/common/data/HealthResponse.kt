package com.adel.common.data

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val service: String,
    val status: String,
    val version: String,
    val timestamp: String,
)