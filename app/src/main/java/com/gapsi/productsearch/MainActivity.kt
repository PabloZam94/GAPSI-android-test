package com.gapsi.productsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gapsi.productsearch.ui.search.SearchRoute
import com.gapsi.productsearch.ui.theme.GapsiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GapsiTheme {
                SearchRoute()
            }
        }
    }
}
