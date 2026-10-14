package com.gapsi.productsearch.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.domain.model.normalizeSearchQuery
import com.gapsi.productsearch.domain.repository.ProductRepository
import com.gapsi.productsearch.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val productRepository: ProductRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * Every submission gets a new [SearchRequest.generation], so searching the same term twice
     * (e.g. after a failure) starts a fresh search instead of being swallowed by StateFlow equality.
     */
    private data class SearchRequest(val term: String, val generation: Long)

    private val query = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val searchRequest = MutableStateFlow(
        SearchRequest(term = savedStateHandle[KEY_SUBMITTED_QUERY] ?: "", generation = 0L),
    )

    /** Survives configuration changes thanks to [cachedIn]; previous searches are cancelled. */
    val products: Flow<PagingData<Product>> = searchRequest
        .flatMapLatest { request ->
            if (request.term.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                productRepository.searchProducts(request.term)
            }
        }
        .cachedIn(viewModelScope)

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        searchRequest,
        searchHistoryRepository.history,
    ) { currentQuery, request, history ->
        SearchUiState(
            query = currentQuery,
            submittedQuery = request.term,
            recentSearches = history.filterMatching(currentQuery),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SearchUiState(query = query.value, submittedQuery = searchRequest.value.term),
    )

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onSearch() = submit(query.value)

    fun onRecentSearchSelected(term: String) = submit(term)

    fun onRemoveRecentSearch(term: String) {
        viewModelScope.launch { searchHistoryRepository.remove(term) }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch { searchHistoryRepository.clear() }
    }

    private fun submit(rawTerm: String) {
        val term = normalizeSearchQuery(rawTerm)
        if (term.isEmpty()) return

        savedStateHandle[KEY_QUERY] = term
        savedStateHandle[KEY_SUBMITTED_QUERY] = term
        searchRequest.value = SearchRequest(term = term, generation = searchRequest.value.generation + 1)
        viewModelScope.launch { searchHistoryRepository.add(term) }
    }

    private fun List<String>.filterMatching(text: String): List<String> {
        val needle = normalizeSearchQuery(text)
        return if (needle.isEmpty()) this else filter { it.contains(needle, ignoreCase = true) }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_SUBMITTED_QUERY = "submitted_query"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
