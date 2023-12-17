package com.kip.reykunyu.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.RichText
import com.kip.reykunyu.ui.components.RichTextComponent
import com.kip.reykunyu.viewmodels.AppPreferenceState
import com.kip.reykunyu.viewmodels.PreferenceViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
    preferenceViewModel: PreferenceViewModel = viewModel(factory = PreferenceViewModel.Factory),
    openNavDrawerAction: () -> Unit = {}
) {
    val noticeText = RichText.create(stringResource(R.string.notice_text))
    val creditsText = RichText.create(stringResource(R.string.credits_text))
    val infoText = RichText.create(stringResource(R.string.appinfo_text))


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
            HorizontalDivider()
        },

        content = { paddingValues ->
            val state: LazyListState = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 10.dp)
            ) {

                item {
                    SectionHeader(title = "Search")
                }

                item {
                    SearchLanguageSelector(
                        display = "Search language",
                        preferenceState = preferenceState,
                        updatePrefAction = { preferenceViewModel.updateSearchLanguage(it) }
                    )
                }

                item {
                    SectionHeader(title = "Info")
                }

                item {
                    if (noticeText != null) {
                        RichTextPanel(title = "Read me!", richText = noticeText)
                    }
                }

                item {
                    if (infoText != null) {
                        RichTextPanel(title = "Info", richText = infoText)
                    }
                }

                item {
                    if (creditsText != null) {
                        RichTextPanel(title = "Privacy & Credits", richText = creditsText)
                    }
                }

            }
        }
    )
}


@Composable
fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    HorizontalDivider(thickness = 2.dp)
    Spacer(modifier = Modifier.padding(vertical = 5.dp))
}

@Composable
fun RichTextPanel(title: String, richText: RichText) {
    Card {
        Column(Modifier.padding(horizontal = 10.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            RichTextComponent(richText = richText, language = Language.English, naviClick = {})
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
        }
    }
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLanguageSelector(
    display: String,
    preferenceState: AppPreferenceState,
    updatePrefAction: (Language) -> Unit
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.padding(10.dp))

            //Label
            Text(display, style = MaterialTheme.typography.titleSmall, fontSize = 20.sp)

            //Selector
            var expanded by remember {
                mutableStateOf(false)
            }

            val selectedLanguage = preferenceState.searchLanguage

            Spacer(Modifier.padding(20.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
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
                        if (selectionOption == Language.Unknown) {
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
                Spacer(Modifier.padding(10.dp))
            }
        }
    }
}