package com.irv205.apimockinterceptor.data.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLoggingInterceptor @Inject constructor(
    private val store: NetworkCallStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val requestHeaders = request.headers.toPairs()
        val requestBody = request.body.toBodyString()

        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            val responseBody = response.peekBody(MAX_BODY_SIZE).string()

            store.add(
                NetworkCall(
                    timestamp = startTime,
                    method = request.method,
                    url = request.url.toString(),
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    statusCode = response.code,
                    responseHeaders = response.headers.toPairs(),
                    responseBody = responseBody,
                    durationMs = duration,
                    error = null,
                ),
            )
            response
        } catch (exception: IOException) {
            store.add(
                NetworkCall(
                    timestamp = startTime,
                    method = request.method,
                    url = request.url.toString(),
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    statusCode = null,
                    responseHeaders = emptyList(),
                    responseBody = null,
                    durationMs = System.currentTimeMillis() - startTime,
                    error = exception.message ?: exception.javaClass.simpleName,
                ),
            )
            throw exception
        }
    }

    private fun Headers.toPairs(): List<Pair<String, String>> =
        (0 until size).map { index -> name(index) to value(index) }

    private fun RequestBody?.toBodyString(): String? {
        if (this == null) return null
        return try {
            val buffer = Buffer()
            writeTo(buffer)
            buffer.readUtf8()
        } catch (exception: Exception) {
            null
        }
    }

    private companion object {
        const val MAX_BODY_SIZE = 1_000_000L
    }
}
