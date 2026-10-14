package com.gapsi.productsearch.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persistent list of the terms the user has searched for, most recent first.
 * Survives process death and app restarts.
 */
interface SearchHistoryRepository {

    val history: Flow<List<String>>

    suspend fun add(term: String)

    suspend fun remove(term: String)

    suspend fun clear()
}
