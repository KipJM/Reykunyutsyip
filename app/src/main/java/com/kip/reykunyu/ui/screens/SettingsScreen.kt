package com.kip.reykunyu.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.viewmodels.AppPreferenceState
import com.kip.reykunyu.viewmodels.PreferenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
    preferenceViewModel: PreferenceViewModel = viewModel(factory = PreferenceViewModel.Factory),
    openNavDrawerAction: () -> Unit = {}
) {

    val preferenceState = preferenceViewModel.preferenceState.collectAsState().value


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openNavDrawerAction) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Reykunyu sidebar menu access"
                        )
                    }
                }
            )
            Divider()
        },

        content = { paddingValues ->
            val state: LazyListState = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.padding(paddingValues)
            ) {

/*                item {
                    LanguageSelector(
                        display = "App language",
                        target = PreferenceViewModel.LanguageRelatedSettings.AppLanguage,
                        preferenceViewModel = preferenceViewModel
                    )
                }*/

                item {
                    SearchLanguageSelector(
                        display = "Search language",
                        preferenceState = preferenceState,
                        updatePrefAction = { preferenceViewModel.updateSearchLanguage(it) }
                    )
                }

            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SearchLanguageSelector(
    display: String,
    preferenceState: AppPreferenceState,
    updatePrefAction: (Language) -> Unit
) {
    Row{
        //Label
        Text(display)

        //Selector
        var expanded by remember {
            mutableStateOf(false)
        }

        val selectedLanguage = preferenceState.searchLanguage


        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded}
        ) {
            Crossfade(targetState = stringResource(selectedLanguage.display)) {
                TextField(
                    // The `menuAnchor` modifier must be passed to the text field for correctness.
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = it,
                    onValueChange = {},
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                Language.values().forEach { selectionOption ->
                    if (selectionOption == Language.Unknown){
                        return@forEach
                    }

                    DropdownMenuItem(
                        text = { Text(stringResource(selectionOption.display)) },
                        onClick = {
                            updatePrefAction(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }

        }
    }
}