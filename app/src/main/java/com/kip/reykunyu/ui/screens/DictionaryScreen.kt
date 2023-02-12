@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.viewmodels.DictSearchState
import com.kip.reykunyu.viewmodels.DictionarySearchViewModel
import com.kip.reykunyu.viewmodels.OfflineDictState
import com.kip.reykunyu.viewmodels.OfflineDictionaryViewModel


//The class for the main dictionary UI
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DictionaryScreen(
    offlineDictViewModel: OfflineDictionaryViewModel = viewModel(),
    searchViewModel: DictionarySearchViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val dictState = offlineDictViewModel.offlineDictState

    val onSearch = {
        focusManager.clearFocus()
        searchViewModel.search()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    DictionarySearchBar(
                        searchString = searchViewModel.searchInput,
                        onInputChanged = {searchViewModel.updateSearchInput(it)},
                        onSearch = onSearch
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { TODO() }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Reykunyu sidebar menu access"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearch ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_description),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            )
        },

        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(paddingValues)
            ) {

                when (dictState) {
                    is OfflineDictState.NotLoaded -> DownloadDictionaryButton {
                        offlineDictViewModel.downloadDictionary()
                    }
                    is OfflineDictState.Loading -> {
                        LoadingView()
                    }
                    is OfflineDictState.Loaded -> {

                        when (val searchState = searchViewModel.dictSearchState) {
                            is DictSearchState.Success -> {
                                StatusText(text = searchState.result.toNavi.size.toString())
                            }
                            DictSearchState.Error -> StatusText(text = "ERROR!")
                            DictSearchState.Loading -> StatusText(text = "LOADING...")
                            DictSearchState.Standby -> {}
                        }
                    }
                    is OfflineDictState.Error -> {
                        StatusText(text = "ERROR!")
                    }
                }
            }
        }
    )


}

@Composable
fun DownloadDictionaryButton(
    clickAction: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = clickAction,
            ) {
            Text(
                text = "Download Dictionary :)",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun LoadingView() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySearchBar(
    searchString: String,
    onInputChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    //Search bar
    OutlinedTextField(
        value = searchString,
        onValueChange = onInputChanged,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        placeholder =
        {
            Text(
                text = stringResource(R.string.search),
                style = MaterialTheme.typography.titleMedium
            )
        },
        textStyle = MaterialTheme.typography.titleMedium,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(32.dp)
    )
}
