package com.irv205.apimockinterceptor.data.mock

data class MockScenario(
    val id: String,
    val name: String,
    val statusCode: Int = 200,
    val contentType: String = "application/json",
    val requestQuery: String = "",
    val requestBody: String = "",
    val responseHeaders: String = "",
    val responseBody: String = "",
    val delayMs: Long = 0L,
) {
    fun matchesRequest(request: okhttp3.Request): Boolean {
        val querySpec = requestQuery.toQueryPairs()
        val bodySpec = requestBody.trim()
        val hasQuery = querySpec.isNotEmpty()
        val hasBody = bodySpec.isNotEmpty()
        if (!hasQuery && !hasBody) return false
        if (hasQuery && !request.matchesQuery(querySpec)) return false
        if (hasBody && !request.matchesBody(bodySpec)) return false
        return true
    }
}
