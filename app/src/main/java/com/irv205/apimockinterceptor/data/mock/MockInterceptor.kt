package com.irv205.apimockinterceptor.data.mock

import com.irv205.apimockinterceptor.data.featureflags.FeatureFlag
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlagsRepository
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInterceptor @Inject constructor(
    private val featureFlags: FeatureFlagsRepository,
    private val repository: MockServicesRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!featureFlags.isEnabled(FeatureFlag.MOCK_SERVICE)) {
            return chain.proceed(request)
        }

        val match = MockRequestMatcher.resolve(repository.services.value, request)
            ?: return chain.proceed(request)

        val scenario = match.scenario
        if (scenario.delayMs > 0) {
            runCatching { Thread.sleep(scenario.delayMs) }
        }

        val contentType = scenario.contentType
            .ifBlank { "application/json" }
            .toMediaTypeOrNull()

        val builder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(scenario.statusCode)
            .message(statusMessage(scenario.statusCode))
            .body(scenario.responseBody.toResponseBody(contentType))
            .header("Content-Type", contentType.toString())
            .header("x-mocked", "true")
            .header("x-mock-service", sanitizeHeaderValue(match.service.name))
            .header("x-mock-scenario", sanitizeHeaderValue(scenario.name))

        scenario.responseHeaders.toHeaderPairs().forEach { (name, value) ->
            builder.header(name, value)
        }

        return builder.build()
    }

    private fun statusMessage(code: Int): String = when (code) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        500 -> "Internal Server Error"
        else -> "Mocked"
    }

    private fun sanitizeHeaderValue(value: String): String =
        value.replace("[^\u0020-\u007e]".toRegex(), "?")
}

private fun String.toHeaderPairs(): List<Pair<String, String>> =
    lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val index = line.indexOf(':')
            if (index <= 0) null
            else line.substring(0, index).trim() to line.substring(index + 1).trim()
        }
        .toList()
