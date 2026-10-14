package com.gapsi.productsearch.data.remote

import com.gapsi.productsearch.domain.error.SearchException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException

/** Translates infrastructure failures into the domain [SearchException] hierarchy. */
internal fun Throwable.toSearchException(): SearchException = when (this) {
    is SearchException -> this
    is MissingApiKeyException -> SearchException.MissingApiKey()
    // SocketTimeoutException and OkHttp call timeouts are both InterruptedIOException.
    is InterruptedIOException -> SearchException.Timeout(this)
    is IOException -> SearchException.NoConnection(this)
    is HttpException -> when (val code = code()) {
        401, 403 -> SearchException.Unauthorized(code)
        429 -> SearchException.RateLimited()
        else -> SearchException.Server(code)
    }
    is SerializationException -> SearchException.UnexpectedResponse(this)
    else -> SearchException.Unknown(this)
}
