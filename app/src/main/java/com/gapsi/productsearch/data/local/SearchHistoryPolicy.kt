package com.gapsi.productsearch.data.local

import com.gapsi.productsearch.domain.model.normalizeSearchQuery

/** Business rules for the search history, kept pure so they are trivially unit-testable. */
internal object SearchHistoryPolicy {

    const val MAX_ENTRIES = 20

    /**
     * Moves [term] to the top of the history. Matching is case-insensitive so "Sony" and "sony"
     * are a single entry, and the list is capped at [maxEntries].
     */
    fun push(history: List<String>, term: String, maxEntries: Int = MAX_ENTRIES): List<String> {
        val normalized = normalizeSearchQuery(term)
        if (normalized.isEmpty()) return history
        return buildList {
            add(normalized)
            history.filterTo(this) { !it.equals(normalized, ignoreCase = true) }
        }.take(maxEntries)
    }

    fun remove(history: List<String>, term: String): List<String> {
        val normalized = normalizeSearchQuery(term)
        return history.filterNot { it.equals(normalized, ignoreCase = true) }
    }
}
