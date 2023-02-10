package com.kip.reykunyu.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kip.reykunyu.ui.screens.DictionaryScreen


//Houses the screens
@Composable
fun ReykunyuApp() {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        DictionaryScreen()
    }
}