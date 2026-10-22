package com.gapsi.productsearch.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gapsi.productsearch.data.remote.WalmartSearchApi
import com.gapsi.productsearch.data.remote.mapper.ProductMapper
import com.gapsi.productsearch.data.remote.toSearchException
import com.gapsi.productsearch.domain.model.Product
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.net.HttpURLConnection

internal class ProductPagingSource(
    private val api: WalmartSearchApi,
    private val mapper: ProductMapper,
    private val query: String,
) : PagingSource<Int, Product>() {
    private val seenIds = HashSet<String>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val result = mapper.map(api.searchByKeyword(keyword = query, page = page))
            val newProducts = result.products.filter { seenIds.add(it.id) }

            val isLastPage = result.products.isEmpty() ||
                (result.totalPages != null && page >= result.totalPages) ||
                page >= MAX_PAGES

            LoadResult.Page(
                data = newProducts,
                prevKey = null,
                nextKey = if (isLastPage) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                // Axesso answers 404 when a keyword/page has no products: treat as end of list.
                LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            } else {
                LoadResult.Error(e.toSearchException())
            }
        } catch (e: Exception) {
            LoadResult.Error(e.toSearchException())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? = null

    companion object {
        const val FIRST_PAGE = 1

        const val MAX_PAGES = 50
    }
}
