package com.gapsi.productsearch.data.remote.mapper

import com.gapsi.productsearch.data.remote.dto.PriceDetailsDto
import com.gapsi.productsearch.data.remote.dto.ProductDto
import com.gapsi.productsearch.data.remote.dto.SearchResponseDto
import com.gapsi.productsearch.domain.model.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** One page of results already mapped to the domain model. */
internal data class SearchPage(
    val products: List<Product>,
    val totalPages: Int?,
)

internal class ProductMapper(private val json: Json) {

    fun map(response: SearchResponseDto): SearchPage {
        val searchResult = response.item?.props?.pageProps?.initialData?.searchResult
            ?: return SearchPage(products = emptyList(), totalPages = null)

        val products = searchResult.itemStacks
            .asSequence()
            .flatMap { it.items }
            .mapNotNull { it.toProductOrNull() }
            .distinctBy { it.id }
            .toList()

        return SearchPage(
            products = products,
            totalPages = searchResult.paginationV2?.maxPage?.takeIf { it > 0 },
        )
    }

    private fun JsonElement.toProductOrNull(): Product? =
        runCatching { json.decodeFromJsonElement(ProductDto.serializer(), this) }
            .getOrNull()
            ?.toDomain()

    private fun ProductDto.toDomain(): Product? {
        val productId = usItemId?.takeIf { it.isNotBlank() } ?: id?.takeIf { it.isNotBlank() } ?: return null
        val title = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null

        val resolvedPrice = price?.takeIf { it > 0.0 }
            ?: priceInfo?.currentPrice?.price?.takeIf { it > 0.0 }
            ?: priceInfo?.currentPrice?.priceString?.parsePrice()
            ?: priceInfo?.priceDetails?.currentPriceValue()?.parsePrice()
            ?: priceInfo?.linePrice?.parsePrice()

        val thumbnail = imageInfo?.thumbnailUrl?.takeIf { it.isNotBlank() }
            ?: image?.takeIf { it.isNotBlank() }

        return Product(
            id = productId,
            title = title,
            price = resolvedPrice,
            thumbnailUrl = thumbnail?.toHttps(),
        )
    }

    private fun PriceDetailsDto.currentPriceValue(): String? =
        priceLines.firstOrNull { it.lineType == LINE_TYPE_CURRENT_PRICE }
            ?.values
            ?.firstOrNull { it.key == VALUE_KEY_PRICE }
            ?.value

    private companion object {
        const val LINE_TYPE_CURRENT_PRICE = "CURRENT_PRICE"
        const val VALUE_KEY_PRICE = "PRICE"

        private val NON_NUMERIC = Regex("[^0-9.]")

        /** "$1,299.00" -> 1299.0 ; "From $5.98" -> 5.98 */
        fun String.parsePrice(): Double? =
            replace(NON_NUMERIC, "").toDoubleOrNull()?.takeIf { it > 0.0 }

        /** Cleartext traffic is disabled by default on API 28+. */
        fun String.toHttps(): String = when {
            startsWith("//") -> "https:$this"
            startsWith("http://") -> "https://" + removePrefix("http://")
            else -> this
        }
    }
}
