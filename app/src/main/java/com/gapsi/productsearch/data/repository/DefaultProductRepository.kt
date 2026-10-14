package com.gapsi.productsearch.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.gapsi.productsearch.data.paging.ProductPagingSource
import com.gapsi.productsearch.data.remote.WalmartSearchApi
import com.gapsi.productsearch.data.remote.mapper.ProductMapper
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

internal class DefaultProductRepository(
    private val api: WalmartSearchApi,
    private val mapper: ProductMapper,
) : ProductRepository {

    override fun searchProducts(query: String): Flow<PagingData<Product>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { ProductPagingSource(api = api, mapper = mapper, query = query) },
        ).flow

    private companion object {
        /** Walmart returns ~40 products per page; the API does not accept a page size. */
        const val PAGE_SIZE = 40
        const val PREFETCH_DISTANCE = 10
    }
}
