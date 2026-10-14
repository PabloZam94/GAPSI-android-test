package com.gapsi.productsearch.domain.error

/**
 * Typed failures the search feature can surface to the UI.
 *
 * The data layer translates transport/parsing errors (OkHttp, Retrofit, kotlinx.serialization)
 * into this hierarchy so the presentation layer never depends on networking libraries.
 */
sealed class SearchException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable? = null) : SearchException("No network connection", cause)

    class Timeout(cause: Throwable? = null) : SearchException("Request timed out", cause)

    class MissingApiKey : SearchException("RAPIDAPI_KEY is not configured")

    class Unauthorized(val code: Int) : SearchException("API key rejected (HTTP $code)")

    class RateLimited : SearchException("API quota exceeded (HTTP 429)")

    class Server(val code: Int) : SearchException("Server error (HTTP $code)")

    class UnexpectedResponse(cause: Throwable? = null) : SearchException("Unexpected response format", cause)

    class Unknown(cause: Throwable? = null) : SearchException("Unknown error", cause)
}
