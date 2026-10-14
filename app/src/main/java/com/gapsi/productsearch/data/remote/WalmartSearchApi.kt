package com.gapsi.productsearch.data.remote

import com.gapsi.productsearch.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Axesso – Walmart Data Service (RapidAPI).
 */
interface WalmartSearchApi {

    @GET("wlm/walmart-search-by-keyword")
    suspend fun searchByKeyword(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("sortBy") sortBy: String = SORT_BEST_MATCH,
    ): SearchResponseDto

    companion object {
        const val SORT_BEST_MATCH = "best_match"
    }
}
