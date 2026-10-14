package com.gapsi.productsearch.ui.search

/**
 * Screen state for everything that is not the paged product list (which is exposed
 * separately as `Flow<PagingData<Product>>`, as recommended by Paging 3).
 *
 * @property query Current text of the search field.
 * @property submittedQuery Last executed search, blank until the user searches for the first time.
 * @property recentSearches Persisted history filtered by [query], most recent first.
 */
data class SearchUiState(
    val query: String = "",
    val submittedQuery: String = "",
    val recentSearches: List<String> = emptyList(),
) {
    val hasSubmittedSearch: Boolean get() = submittedQuery.isNotBlank()
}
