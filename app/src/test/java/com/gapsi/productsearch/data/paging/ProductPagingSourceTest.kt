package com.gapsi.productsearch.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import com.gapsi.productsearch.data.remote.WalmartSearchApi
import com.gapsi.productsearch.data.remote.dto.InitialDataDto
import com.gapsi.productsearch.data.remote.dto.ItemStackDto
import com.gapsi.productsearch.data.remote.dto.PagePropsDto
import com.gapsi.productsearch.data.remote.dto.PaginationDto
import com.gapsi.productsearch.data.remote.dto.ProductDto
import com.gapsi.productsearch.data.remote.dto.PropsDto
import com.gapsi.productsearch.data.remote.dto.SearchItemDto
import com.gapsi.productsearch.data.remote.dto.SearchResponseDto
import com.gapsi.productsearch.data.remote.dto.SearchResultDto
import com.gapsi.productsearch.data.remote.mapper.ProductMapper
import com.gapsi.productsearch.domain.error.SearchException
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.testing.testJson
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ProductPagingSourceTest {

    private val api = FakeWalmartSearchApi()

    private fun pagingSource(query: String = "nintendo") =
        ProductPagingSource(api = api, mapper = ProductMapper(testJson), query = query)

    @Test
    fun `refresh loads first page and points to the next one`() = runTest {
        api.pages[1] = response(maxPage = 3, "1", "2")

        val result = pagingSource().load(refresh()) as LoadResult.Page

        assertEquals(listOf("1", "2"), result.data.map(Product::id))
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
        assertEquals(listOf("nintendo" to 1), api.requests)
    }

    @Test
    fun `last page reported by the api ends pagination`() = runTest {
        api.pages[3] = response(maxPage = 3, "9")

        val result = pagingSource().load(append(key = 3)) as LoadResult.Page

        assertNull(result.nextKey)
    }

    @Test
    fun `empty page ends pagination`() = runTest {
        api.pages[1] = response(maxPage = null)

        val result = pagingSource().load(refresh()) as LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `products repeated in later pages are dropped`() = runTest {
        api.pages[1] = response(maxPage = 5, "1", "2")
        api.pages[2] = response(maxPage = 5, "2", "3")
        val source = pagingSource()

        source.load(refresh())
        val second = source.load(append(key = 2)) as LoadResult.Page

        assertEquals(listOf("3"), second.data.map(Product::id))
        assertEquals(3, second.nextKey)
    }

    @Test
    fun `http 404 is treated as no more results`() = runTest {
        api.error = HttpException(Response.error<Any>(404, "".toResponseBody(null)))

        val result = pagingSource().load(refresh()) as LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `network failure is exposed as typed error`() = runTest {
        api.error = IOException("offline")

        val result = pagingSource().load(refresh()) as LoadResult.Error

        assertTrue(result.throwable is SearchException.NoConnection)
    }

    @Test
    fun `quota exceeded is exposed as typed error`() = runTest {
        api.error = HttpException(Response.error<Any>(429, "".toResponseBody(null)))

        val result = pagingSource().load(refresh()) as LoadResult.Error

        assertTrue(result.throwable is SearchException.RateLimited)
    }

    private fun refresh() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = 40,
        placeholdersEnabled = false,
    )

    private fun append(key: Int) = PagingSource.LoadParams.Append(
        key = key,
        loadSize = 40,
        placeholdersEnabled = false,
    )

    private fun response(maxPage: Int?, vararg ids: String) = SearchResponseDto(
        item = SearchItemDto(
            PropsDto(
                PagePropsDto(
                    InitialDataDto(
                        SearchResultDto(
                            itemStacks = listOf(
                                ItemStackDto(
                                    items = ids.map { id ->
                                        testJson.encodeToJsonElement(
                                            ProductDto.serializer(),
                                            ProductDto(usItemId = id, name = "Product $id", price = 10.0),
                                        )
                                    },
                                ),
                            ),
                            paginationV2 = PaginationDto(maxPage = maxPage),
                        ),
                    ),
                ),
            ),
        ),
    )

    private class FakeWalmartSearchApi : WalmartSearchApi {
        val pages = mutableMapOf<Int, SearchResponseDto>()
        val requests = mutableListOf<Pair<String, Int>>()
        var error: Throwable? = null

        override suspend fun searchByKeyword(keyword: String, page: Int, sortBy: String): SearchResponseDto {
            requests += keyword to page
            error?.let { throw it }
            return pages[page] ?: SearchResponseDto()
        }
    }
}
