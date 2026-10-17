package com.irv205.apimockinterceptor.data.network

data class NetworkCall(
    val id: Long = 0,
    val timestamp: Long,
    val method: String,
    val url: String,
    val requestHeaders: List<Pair<String, String>>,
    val requestBody: String?,
    val statusCode: Int?,
    val responseHeaders: List<Pair<String, String>>,
    val responseBody: String?,
    val durationMs: Long,
    val error: String?,
)
