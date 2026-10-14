package com.gapsi.productsearch.domain.model

private val WHITESPACE = Regex("\\s+")

/** Canonical form of a user-typed query: trimmed, single-spaced. "  nintendo   switch " -> "nintendo switch". */
fun normalizeSearchQuery(raw: String): String = raw.trim().replace(WHITESPACE, " ")
