package com.gapsi.productsearch.testing

import kotlinx.serialization.json.Json

/** Same configuration as production (see networkModule). */
val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = true
}

fun readResource(name: String): String =
    requireNotNull(object {}.javaClass.classLoader?.getResource(name)) { "Missing test resource $name" }
        .readText()
