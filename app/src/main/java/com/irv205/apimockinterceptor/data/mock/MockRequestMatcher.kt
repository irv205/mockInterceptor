package com.irv205.apimockinterceptor.data.mock

import okhttp3.Request
import okio.Buffer

data class MockMatch(
    val service: MockService,
    val scenario: MockScenario,
)

object MockRequestMatcher {

    private val REGEX_MARKERS = charArrayOf('\\', '(', ')', '[', ']', '|', '*', '+', '?', '^', '$')

    fun resolve(services: List<MockService>, request: Request): MockMatch? {
        val candidates = services.filter { it.enabled && it.matchesMethodAndPath(request) }
        if (candidates.isEmpty()) return null

        for (service in candidates) {
            for (scenario in service.scenarios) {
                if (scenario.matchesRequest(request)) {
                    return MockMatch(service, scenario)
                }
            }
        }

        val service = candidates.first()
        val scenario = service.selectedScenario() ?: return null
        return MockMatch(service, scenario)
    }

    fun MockService.matchesMethodAndPath(request: Request): Boolean {
        if (!method.equals(request.method, ignoreCase = true)) return false
        return compilePattern(pathPattern)?.containsMatchIn(request.url.encodedPath) == true
    }

    fun compilePattern(pattern: String): Regex? {
        val trimmed = pattern.trim()
        if (trimmed.isEmpty()) return null
        val normalized = if (trimmed.startsWith("/")) trimmed else "/$trimmed"

        if (normalized.any { it in REGEX_MARKERS }) {
            return runCatching { Regex(normalized + "$") }.getOrNull()
        }

        val builder = StringBuilder()
        var i = 0
        while (i < normalized.length) {
            val c = normalized[i]
            if (c == '{') {
                val end = normalized.indexOf('}', startIndex = i)
                if (end > i) {
                    builder.append("[^/]+")
                    i = end + 1
                    continue
                }
            }
            if (c.isLetterOrDigit() || c == '/' || c == '-' || c == '_') {
                builder.append(c)
            } else {
                builder.append('\\').append(c)
            }
            i++
        }
        return runCatching { Regex(builder.toString() + "$") }.getOrNull()
    }
}

fun String.toQueryPairs(): List<Pair<String, String>> =
    trim()
        .split('&', '\n', '\r')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { part ->
            val index = part.indexOf('=')
            if (index <= 0) null
            else part.substring(0, index).trim() to part.substring(index + 1).trim()
        }

fun Request.matchesQuery(pairs: List<Pair<String, String>>): Boolean =
    pairs.all { (key, value) -> url.queryParameter(key) == value }

fun Request.matchesBody(contains: String): Boolean {
    val requestBody = body ?: return false
    val buffer = Buffer()
    return runCatching {
        requestBody.writeTo(buffer)
        buffer.readUtf8().contains(contains)
    }.getOrDefault(false)
}
