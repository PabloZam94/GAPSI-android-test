package com.gapsi.productsearch.domain.repository

import androidx.paging.PagingData
import com.gapsi.productsearch.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    /**
     * Returns a paged stream of products matching [query]. New pages are requested
     * lazily as the user scrolls.
     */
    fun searchProducts(query: String): Flow<PagingData<Product>>
}
