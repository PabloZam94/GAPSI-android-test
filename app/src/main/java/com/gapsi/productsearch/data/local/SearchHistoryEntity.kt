package com.gapsi.productsearch.data.local

import kotlinx.serialization.Serializable

/** On-disk representation of the search history (stored as JSON through DataStore). */
@Serializable
internal data class SearchHistoryEntity(
    val terms: List<String> = emptyList(),
)
