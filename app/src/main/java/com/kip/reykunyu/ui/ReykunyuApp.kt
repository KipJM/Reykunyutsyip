package com.kip.reykunyu.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.ui.screens.DictionaryScreen
import com.kip.reykunyu.viewmodels.DictionarySearchViewModel
import com.kip.reykunyu.viewmodels.OfflineDictionaryViewModel


//Houses the screens
@Composable
fun ReykunyuApp() {
    val offlineDictViewModel: OfflineDictionaryViewModel = viewModel()
    val searchViewModel: DictionarySearchViewModel = viewModel()


    Surface(
    ) {

        DictionaryScreen(offlineDictViewModel, searchViewModel)
    }
}