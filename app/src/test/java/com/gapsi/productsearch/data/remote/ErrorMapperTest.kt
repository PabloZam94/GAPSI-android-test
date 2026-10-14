package com.gapsi.productsearch.data.remote

import com.gapsi.productsearch.domain.error.SearchException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun `maps connectivity errors`() {
        assertTrue(UnknownHostException().toSearchException() is SearchException.NoConnection)
        assertTrue(IOException().toSearchException() is SearchException.NoConnection)
        assertTrue(SocketTimeoutException().toSearchException() is SearchException.Timeout)
    }

    @Test
    fun `missing api key takes precedence over generic io errors`() {
        assertTrue(MissingApiKeyException().toSearchException() is SearchException.MissingApiKey)
    }

    @Test
    fun `maps http status codes`() {
        assertTrue(http(401).toSearchException() is SearchException.Unauthorized)
        assertTrue(http(403).toSearchException() is SearchException.Unauthorized)
        assertTrue(http(429).toSearchException() is SearchException.RateLimited)
        assertEquals(503, (http(503).toSearchException() as SearchException.Server).code)
    }

    @Test
    fun `maps parsing and unknown errors`() {
        assertTrue(SerializationException("bad").toSearchException() is SearchException.UnexpectedResponse)
        assertTrue(IllegalStateException().toSearchException() is SearchException.Unknown)
    }
}
