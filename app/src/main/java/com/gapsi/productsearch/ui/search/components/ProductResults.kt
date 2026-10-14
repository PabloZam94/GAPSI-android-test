package com.gapsi.productsearch.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.gapsi.productsearch.R
import com.gapsi.productsearch.domain.model.Product

/**
 * Renders every state of the paged search: first load, first-load error, no results,
 * and the list itself with append loading / append error / end-of-list footers.
 */
@Composable
fun ProductResults(
    query: String,
    products: LazyPagingItems<Product>,
    modifier: Modifier = Modifier,
) {
    val refreshState = products.loadState.refresh
    val isEmpty = products.itemCount == 0

    when {
        isEmpty && refreshState is LoadState.Loading -> FullScreenLoading(modifier)

        isEmpty && refreshState is LoadState.Error -> StatusMessage(
            iconRes = R.drawable.ic_cloud_off,
            title = stringResource(R.string.state_error_title),
            message = refreshState.error.toUserMessage(),
            actionLabel = stringResource(R.string.action_retry),
            onAction = products::retry,
            modifier = modifier,
        )

        // endOfPaginationReached avoids flashing "no results" before the first load starts.
        isEmpty && refreshState is LoadState.NotLoading && products.loadState.append.endOfPaginationReached ->
            StatusMessage(
                iconRes = R.drawable.ic_search,
                title = stringResource(R.string.state_empty_title),
                message = stringResource(R.string.state_empty_message, query),
                modifier = modifier,
            )

        isEmpty -> FullScreenLoading(modifier)

        else -> ProductList(products = products, modifier = modifier)
    }
}

@Composable
private fun ProductList(
    products: LazyPagingItems<Product>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                count = products.itemCount,
                key = products.itemKey { it.id },
                contentType = products.itemContentType { CONTENT_TYPE_PRODUCT },
            ) { index ->
                products[index]?.let { product -> ProductCard(product = product) }
            }

            item(key = KEY_FOOTER, contentType = KEY_FOOTER) {
                when (val append = products.loadState.append) {
                    is LoadState.Loading -> LoadingFooter()
                    is LoadState.Error -> ErrorFooter(error = append.error, onRetry = products::retry)
                    is LoadState.NotLoading -> if (append.endOfPaginationReached) EndOfListFooter()
                }
            }
        }

        // Pull-to-refresh style indicator when a refresh happens with content already on screen.
        if (products.loadState.refresh is LoadState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            )
        }
    }
}

private const val CONTENT_TYPE_PRODUCT = "product"
private const val KEY_FOOTER = "footer"
