package com.gapsi.productsearch.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gapsi.productsearch.R

@Composable
fun RecentSearches(
    terms: List<String>,
    onTermClick: (String) -> Unit,
    onRemoveTerm: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onClearAll) {
                    Text(stringResource(R.string.history_clear_all))
                }
            }
        }
        items(items = terms, key = { it }) { term ->
            ListItem(
                modifier = Modifier
                    .animateItem()
                    .clickable { onTermClick(term) },
                headlineContent = {
                    Text(text = term, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                leadingContent = {
                    Icon(painter = painterResource(R.drawable.ic_history), contentDescription = null)
                },
                trailingContent = {
                    IconButton(onClick = { onRemoveTerm(term) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.history_remove_item, term),
                        )
                    }
                },
            )
        }
    }
}
