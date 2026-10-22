package com.gapsi.productsearch.ui.search

data class SearchUiState(
    val query: String = "",
    val submittedQuery: String = "",
    val recentSearches: List<String> = emptyList(),
) {
    val hasSubmittedSearch: Boolean get() = submittedQuery.isNotBlank()
}
