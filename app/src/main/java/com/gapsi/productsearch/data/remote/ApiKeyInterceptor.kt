package com.gapsi.productsearch.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Signs every request with the RapidAPI credentials, keeping them out of the API interface.
 *
 * Fails fast with [MissingApiKeyException] when the key was not provided at build time, so the
 * user gets an actionable message instead of a generic 401.
 */
internal class ApiKeyInterceptor(
    private val apiKey: String,
    private val apiHost: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (apiKey.isBlank()) throw MissingApiKeyException()

        val request = chain.request().newBuilder()
            .header(HEADER_API_KEY, apiKey)
            .header(HEADER_API_HOST, apiHost)
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER_API_KEY = "x-rapidapi-key"
        const val HEADER_API_HOST = "x-rapidapi-host"
    }
}

/** Must extend [IOException]: OkHttp only propagates IOExceptions thrown by interceptors safely. */
internal class MissingApiKeyException : IOException("RAPIDAPI_KEY is not configured")
