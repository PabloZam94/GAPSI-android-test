package com.gapsi.productsearch.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gapsi.productsearch.R
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.ui.search.components.ProductResults
import com.gapsi.productsearch.ui.search.components.RecentSearches
import com.gapsi.productsearch.ui.search.components.SearchField
import com.gapsi.productsearch.ui.search.components.StatusMessage
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: wires the ViewModel to the stateless [SearchScreen]. */
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val products = viewModel.products.collectAsLazyPagingItems()

    SearchScreen(
        uiState = uiState,
        products = products,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onRecentSearchClick = viewModel::onRecentSearchSelected,
        onRemoveRecentSearch = viewModel::onRemoveRecentSearch,
        onClearRecentSearches = viewModel::onClearRecentSearches,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    products: LazyPagingItems<Product>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFieldFocused by remember { mutableStateOf(false) }

    val dismissKeyboard = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // While typing over existing results, Back closes the suggestions instead of the app.
    BackHandler(enabled = isSearchFieldFocused && uiState.hasSubmittedSearch) { dismissKeyboard() }

    val showRecentSearches = uiState.recentSearches.isNotEmpty() &&
        (isSearchFieldFocused || !uiState.hasSubmittedSearch)

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onSearch = {
                    onSearch()
                    dismissKeyboard()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged { isSearchFieldFocused = it.isFocused },
            )

            when {
                showRecentSearches -> RecentSearches(
                    terms = uiState.recentSearches,
                    onTermClick = { term ->
                        onRecentSearchClick(term)
                        dismissKeyboard()
                    },
                    onRemoveTerm = onRemoveRecentSearch,
                    onClearAll = onClearRecentSearches,
                    modifier = Modifier.fillMaxSize(),
                )

                !uiState.hasSubmittedSearch -> StatusMessage(
                    iconRes = R.drawable.ic_search,
                    title = stringResource(R.string.state_welcome_title),
                    message = stringResource(R.string.state_welcome_message),
                )

                // Keyed by the query so every new search starts scrolled to the top.
                else -> key(uiState.submittedQuery) {
                    ProductResults(
                        query = uiState.submittedQuery,
                        products = products,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
