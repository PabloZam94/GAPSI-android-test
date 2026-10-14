package com.gapsi.productsearch.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.gapsi.productsearch.BuildConfig
import com.gapsi.productsearch.data.local.SearchHistoryEntity
import com.gapsi.productsearch.data.local.SearchHistorySerializer
import com.gapsi.productsearch.data.remote.ApiKeyInterceptor
import com.gapsi.productsearch.data.remote.WalmartSearchApi
import com.gapsi.productsearch.data.remote.mapper.ProductMapper
import com.gapsi.productsearch.data.repository.DataStoreSearchHistoryRepository
import com.gapsi.productsearch.data.repository.DefaultProductRepository
import com.gapsi.productsearch.domain.repository.ProductRepository
import com.gapsi.productsearch.domain.repository.SearchHistoryRepository
import com.gapsi.productsearch.ui.search.SearchViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 30L
private const val SEARCH_HISTORY_FILE = "search_history.json"

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            isLenient = true
        }
    }

    single {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(
                ApiKeyInterceptor(apiKey = BuildConfig.RAPIDAPI_KEY, apiHost = BuildConfig.RAPIDAPI_HOST),
            )

        if (BuildConfig.DEBUG) {
            // BASIC: the Walmart payload is several hundred KB, logging bodies would flood Logcat.
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader(ApiKeyInterceptor.HEADER_API_KEY)
                },
            )
        }
        builder.build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<WalmartSearchApi> { get<Retrofit>().create(WalmartSearchApi::class.java) }
}

val dataModule = module {
    single { ProductMapper(json = get()) }

    single<ProductRepository> { DefaultProductRepository(api = get(), mapper = get()) }

    single<DataStore<SearchHistoryEntity>> {
        DataStoreFactory.create(
            serializer = SearchHistorySerializer(json = get()),
            corruptionHandler = ReplaceFileCorruptionHandler { SearchHistoryEntity() },
            produceFile = { androidContext().dataStoreFile(SEARCH_HISTORY_FILE) },
        )
    }

    single<SearchHistoryRepository> { DataStoreSearchHistoryRepository(dataStore = get()) }
}

val presentationModule = module {
    viewModel {
        SearchViewModel(
            productRepository = get(),
            searchHistoryRepository = get(),
            savedStateHandle = get(),
        )
    }
}

val appModules = listOf(networkModule, dataModule, presentationModule)
