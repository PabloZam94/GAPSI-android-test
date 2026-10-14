package com.gapsi.productsearch.data.repository

import androidx.datastore.core.DataStore
import com.gapsi.productsearch.data.local.SearchHistoryEntity
import com.gapsi.productsearch.data.local.SearchHistoryPolicy
import com.gapsi.productsearch.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * [SearchHistoryRepository] backed by a typed DataStore file, so history survives app restarts.
 * DataStore provides atomic, transactional writes and is main-safe.
 */
internal class DataStoreSearchHistoryRepository(
    private val dataStore: DataStore<SearchHistoryEntity>,
) : SearchHistoryRepository {

    override val history: Flow<List<String>> = dataStore.data
        .map { it.terms }
        .catch { error ->
            // An unreadable file must never crash the UI; history is non-critical data.
            if (error is IOException) emit(emptyList()) else throw error
        }
        .distinctUntilChanged()

    override suspend fun add(term: String) {
        dataStore.updateData { it.copy(terms = SearchHistoryPolicy.push(it.terms, term)) }
    }

    override suspend fun remove(term: String) {
        dataStore.updateData { it.copy(terms = SearchHistoryPolicy.remove(it.terms, term)) }
    }

    override suspend fun clear() {
        dataStore.updateData { it.copy(terms = emptyList()) }
    }
}
