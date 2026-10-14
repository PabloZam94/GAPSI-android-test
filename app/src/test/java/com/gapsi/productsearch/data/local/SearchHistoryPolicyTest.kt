package com.gapsi.productsearch.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHistoryPolicyTest {

    @Test
    fun `new term goes first`() {
        assertEquals(listOf("sony", "nintendo"), SearchHistoryPolicy.push(listOf("nintendo"), "sony"))
    }

    @Test
    fun `repeated term moves to the top without duplicating, ignoring case`() {
        val history = listOf("xbox", "Sony", "nintendo")

        assertEquals(listOf("sony", "xbox", "nintendo"), SearchHistoryPolicy.push(history, "sony"))
    }

    @Test
    fun `term is normalized before storing`() {
        assertEquals(listOf("nintendo switch"), SearchHistoryPolicy.push(emptyList(), "  nintendo   switch "))
    }

    @Test
    fun `blank terms are ignored`() {
        assertEquals(listOf("sony"), SearchHistoryPolicy.push(listOf("sony"), "   "))
    }

    @Test
    fun `history is capped`() {
        val history = (1..5).map { "term $it" }

        val result = SearchHistoryPolicy.push(history, "new", maxEntries = 3)

        assertEquals(listOf("new", "term 1", "term 2"), result)
    }

    @Test
    fun `remove ignores case`() {
        assertEquals(listOf("xbox"), SearchHistoryPolicy.remove(listOf("Sony", "xbox"), "sony"))
    }
}
