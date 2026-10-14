package com.gapsi.productsearch.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.domain.repository.ProductRepository
import com.gapsi.productsearch.domain.repository.SearchHistoryRepository
import com.gapsi.productsearch.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val productRepository = FakeProductRepository()
    private val historyRepository = FakeSearchHistoryRepository()

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        SearchViewModel(productRepository, historyRepository, savedStateHandle)

    /** Paging and UI state flows are cold/WhileSubscribed: keep a collector alive like the UI does. */
    private fun TestScope.observe(viewModel: SearchViewModel) {
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.products.collect {} }
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.uiState.collect {} }
    }

    @Test
    fun `search normalizes the query, triggers it and stores it in history`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onQueryChange("  nintendo   switch ")
        viewModel.onSearch()

        assertEquals(listOf("nintendo switch"), productRepository.queries)
        assertEquals(listOf("nintendo switch"), historyRepository.history.value)
        assertEquals("nintendo switch", viewModel.uiState.value.submittedQuery)
        assertEquals("nintendo switch", viewModel.uiState.value.query)
    }

    @Test
    fun `blank query is ignored`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onQueryChange("   ")
        viewModel.onSearch()

        assertTrue(productRepository.queries.isEmpty())
        assertTrue(historyRepository.history.value.isEmpty())
        assertTrue(!viewModel.uiState.value.hasSubmittedSearch)
    }

    @Test
    fun `searching the same term again starts a new search`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onQueryChange("sony")
        viewModel.onSearch()
        viewModel.onSearch()

        assertEquals(listOf("sony", "sony"), productRepository.queries)
    }

    @Test
    fun `selecting a recent search runs it and fills the field`() = runTest {
        historyRepository.history.value = listOf("xbox", "sony")
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onRecentSearchSelected("sony")

        assertEquals(listOf("sony"), productRepository.queries)
        assertEquals("sony", viewModel.uiState.value.query)
        assertEquals(listOf("sony", "xbox"), historyRepository.history.value)
    }

    @Test
    fun `recent searches are filtered by the typed text`() = runTest {
        historyRepository.history.value = listOf("nintendo switch", "sony tv", "Sony headphones")
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onQueryChange("son")

        assertEquals(listOf("sony tv", "Sony headphones"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `remove and clear update history`() = runTest {
        historyRepository.history.value = listOf("xbox", "sony", "lego")
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onRemoveRecentSearch("sony")
        assertEquals(listOf("xbox", "lego"), viewModel.uiState.value.recentSearches)

        viewModel.onClearRecentSearches()
        assertTrue(viewModel.uiState.value.recentSearches.isEmpty())
    }

    @Test
    fun `submitted search is restored after process death`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("submitted_query" to "lego", "query" to "lego")))
        observe(viewModel)

        assertEquals(listOf("lego"), productRepository.queries)
        assertEquals("lego", viewModel.uiState.value.submittedQuery)
    }

    private class FakeProductRepository : ProductRepository {
        val queries = mutableListOf<String>()

        override fun searchProducts(query: String): Flow<PagingData<Product>> {
            queries += query
            return flowOf(PagingData.empty())
        }
    }

    private class FakeSearchHistoryRepository : SearchHistoryRepository {
        override val history = MutableStateFlow<List<String>>(emptyList())

        override suspend fun add(term: String) {
            history.value = listOf(term) + history.value.filterNot { it.equals(term, ignoreCase = true) }
        }

        override suspend fun remove(term: String) {
            history.value = history.value.filterNot { it.equals(term, ignoreCase = true) }
        }

        override suspend fun clear() {
            history.value = emptyList()
        }
    }
}
