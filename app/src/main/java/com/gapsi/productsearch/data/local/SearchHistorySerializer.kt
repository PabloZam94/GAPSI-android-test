package com.gapsi.productsearch.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal class SearchHistorySerializer(private val json: Json) : Serializer<SearchHistoryEntity> {

    override val defaultValue: SearchHistoryEntity = SearchHistoryEntity()

    override suspend fun readFrom(input: InputStream): SearchHistoryEntity =
        try {
            json.decodeFromString(SearchHistoryEntity.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read search history", e)
        }

    override suspend fun writeTo(t: SearchHistoryEntity, output: OutputStream) {
        output.write(json.encodeToString(SearchHistoryEntity.serializer(), t).encodeToByteArray())
    }
}
