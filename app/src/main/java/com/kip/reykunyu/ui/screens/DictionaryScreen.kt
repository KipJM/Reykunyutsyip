package com.kip.reykunyu.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kip.reykunyu.ui.screens.DictionaryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Dictionary


//The class for the main dictionary UI
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.dictLoadingState) {
        LoadingState.Standby -> DownloadDictionaryButton {
            viewModel.downloadDictionary()
        }
        LoadingState.Loading -> {
            StatusText(text = "Loading...")
        }
        LoadingState.Success -> {
            Text(uiState.debugJsonString)
        }
        LoadingState.Error -> {
            StatusText(text = "ERROR!")
        }
    }
}

@Composable
fun DownloadDictionaryButton(
    clickAction: () -> Unit
) {
    Button(
        onClick = clickAction
    ) {
        Text(text = "Download Dictionary :)")
    }
}


@Composable
fun StatusText(
    text: String,
    modifier: Modifier = Modifier
){
    Text(
        text = text,
        modifier = modifier
    )
}


@Preview
@Composable
fun PreviewDictionaryScreen() {
    DictionaryScreen()
}