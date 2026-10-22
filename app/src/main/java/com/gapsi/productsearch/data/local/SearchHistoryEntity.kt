package com.gapsi.productsearch.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchHistoryEntity(
    val terms: List<String> = emptyList(),
)
