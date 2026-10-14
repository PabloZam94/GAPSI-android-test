package com.gapsi.productsearch.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*
 * Axesso proxies Walmart's server-side rendered payload, which is huge and not formally versioned.
 * Only the fields the app needs are modelled; every field is optional so a missing node degrades
 * gracefully instead of failing the whole page.
 *
 * Path: item.props.pageProps.initialData.searchResult.{itemStacks[].items[], paginationV2.maxPage}
 */

@Serializable
data class SearchResponseDto(
    val responseStatus: String? = null,
    val item: SearchItemDto? = null,
)

@Serializable
data class SearchItemDto(val props: PropsDto? = null)

@Serializable
data class PropsDto(val pageProps: PagePropsDto? = null)

@Serializable
data class PagePropsDto(val initialData: InitialDataDto? = null)

@Serializable
data class InitialDataDto(val searchResult: SearchResultDto? = null)

@Serializable
data class SearchResultDto(
    val itemStacks: List<ItemStackDto> = emptyList(),
    val paginationV2: PaginationDto? = null,
)

/**
 * Items are kept as raw [JsonElement] and decoded one by one in the mapper: Walmart mixes
 * products with ads/placeholders of different shapes, and one malformed entry must not
 * discard the rest of the page.
 */
@Serializable
data class ItemStackDto(val items: List<JsonElement> = emptyList())

@Serializable
data class PaginationDto(val maxPage: Int? = null)

@Serializable
data class ProductDto(
    val id: String? = null,
    val usItemId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val image: String? = null,
    val imageInfo: ImageInfoDto? = null,
    val priceInfo: PriceInfoDto? = null,
)

@Serializable
data class ImageInfoDto(val thumbnailUrl: String? = null)

@Serializable
data class PriceInfoDto(
    val linePrice: String? = null,
    val currentPrice: CurrentPriceDto? = null,
    val priceDetails: PriceDetailsDto? = null,
)

/** e.g. priceLines: [{ lineType: "CURRENT_PRICE", values: [{ key: "PRICE", value: "499.00" }] }] */
@Serializable
data class PriceDetailsDto(val priceLines: List<PriceLineDto> = emptyList())

@Serializable
data class PriceLineDto(
    val lineType: String? = null,
    val values: List<PriceValueDto> = emptyList(),
)

@Serializable
data class PriceValueDto(
    val key: String? = null,
    val value: String? = null,
)

@Serializable
data class CurrentPriceDto(
    val price: Double? = null,
    val priceString: String? = null,
)
