package com.kip.reykunyu.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kip.reykunyu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    openNavDrawerAction: () -> Unit = {}
) {


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
                item {

                }

            }
        }
    )
}
