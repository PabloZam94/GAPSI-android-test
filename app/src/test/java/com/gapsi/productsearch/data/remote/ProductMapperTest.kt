package com.gapsi.productsearch.data.remote

import com.gapsi.productsearch.data.remote.dto.SearchResponseDto
import com.gapsi.productsearch.data.remote.mapper.ProductMapper
import com.gapsi.productsearch.domain.model.Product
import com.gapsi.productsearch.testing.readResource
import com.gapsi.productsearch.testing.testJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductMapperTest {

    private val mapper = ProductMapper(testJson)

    private fun mapFixture() = mapper.map(
        testJson.decodeFromString(SearchResponseDto.serializer(), readResource("search_response.json")),
    )

    @Test
    fun `maps products skipping ads, malformed entries and duplicates`() {
        val page = mapFixture()

        assertEquals(listOf("493824815", "111", "222", "ABC"), page.products.map(Product::id))
        assertEquals(25, page.totalPages)
    }

    @Test
    fun `prefers thumbnail url and trims title`() {
        val product = mapFixture().products.first()

        assertEquals("Nintendo Switch OLED Model w/ White Joy-Con", product.title)
        assertEquals(349.0, product.price!!, 0.0)
        assertEquals("https://i5.walmartimages.com/thumb.jpeg", product.thumbnailUrl)
    }

    @Test
    fun `falls back to current price and upgrades image to https`() {
        val product = mapFixture().products.first { it.id == "111" }

        assertEquals(49.99, product.price!!, 0.0)
        assertEquals("https://i5.walmartimages.com/mario.jpeg", product.thumbnailUrl)
    }

    @Test
    fun `parses formatted line price and protocol relative urls`() {
        val product = mapFixture().products.first { it.id == "222" }

        assertEquals(1069.50, product.price!!, 0.0)
        assertEquals("https://i5.walmartimages.com/pro.jpeg", product.thumbnailUrl)
    }

    @Test
    fun `product without price or image keeps nulls`() {
        val product = mapFixture().products.first { it.id == "ABC" }

        assertNull(product.price)
        assertNull(product.thumbnailUrl)
    }

    @Test
    fun `empty payload yields empty page`() {
        val page = mapper.map(SearchResponseDto(responseStatus = "PRODUCT_NOT_FOUND_RESPONSE"))

        assertTrue(page.products.isEmpty())
        assertNull(page.totalPages)
    }
}
