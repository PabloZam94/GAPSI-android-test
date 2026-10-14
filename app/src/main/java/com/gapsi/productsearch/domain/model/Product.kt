package com.gapsi.productsearch.domain.model

/**
 * A catalog product as consumed by the presentation layer.
 *
 * @property id Stable product identifier, unique within a search.
 * @property title Human readable product name.
 * @property price Current price in USD, or `null` when the catalog does not expose one.
 * @property thumbnailUrl Absolute URL of the product thumbnail, if any.
 */
data class Product(
    val id: String,
    val title: String,
    val price: Double?,
    val thumbnailUrl: String?,
)
