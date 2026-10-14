package com.gapsi.productsearch.ui.search.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gapsi.productsearch.R
import com.gapsi.productsearch.domain.error.SearchException
import com.gapsi.productsearch.ui.theme.GapsiTheme

/** Centered icon + title + message + optional action, used for welcome, empty and error states. */
@Composable
fun StatusMessage(
    @DrawableRes iconRes: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Footer shown at the end of the list while the next page loads or when it fails. */
@Composable
fun LoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
fun ErrorFooter(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = error.toUserMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
fun EndOfListFooter(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.state_end_of_list),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

/** Maps domain failures to localized, actionable copy. */
@Composable
fun Throwable.toUserMessage(): String = when (this) {
    is SearchException.NoConnection -> stringResource(R.string.error_no_connection)
    is SearchException.Timeout -> stringResource(R.string.error_timeout)
    is SearchException.MissingApiKey -> stringResource(R.string.error_missing_api_key)
    is SearchException.Unauthorized -> stringResource(R.string.error_unauthorized)
    is SearchException.RateLimited -> stringResource(R.string.error_rate_limited)
    is SearchException.Server -> stringResource(R.string.error_server, code)
    is SearchException.UnexpectedResponse -> stringResource(R.string.error_unexpected_response)
    else -> stringResource(R.string.error_unknown)
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    GapsiTheme {
        StatusMessage(
            iconRes = R.drawable.ic_cloud_off,
            title = stringResource(R.string.state_error_title),
            message = SearchException.NoConnection().toUserMessage(),
            actionLabel = stringResource(R.string.action_retry),
            onAction = {},
        )
    }
}
