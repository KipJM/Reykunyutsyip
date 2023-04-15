package com.kip.reykunyu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.ui.screens.DictionaryScreen
import com.kip.reykunyu.ui.screens.SettingsScreen
import com.kip.reykunyu.viewmodels.*
import kotlinx.coroutines.launch


//Houses the screens
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReykunyuContainer() {
    val appViewModel: AppViewModel = viewModel()

    val offlineDictViewModel: OfflineDictionaryViewModel = viewModel(
        factory = OfflineDictionaryViewModel.Factory
    )
    val searchViewModel: DictionarySearchViewModel = viewModel()
    val preferenceViewModel: PreferenceViewModel = viewModel(
        factory = PreferenceViewModel.Factory
    )


    Surface ()
    {
        val drawerState = rememberDrawerState(initialValue = appViewModel.navDrawerVisibility)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(28.dp))
                    //LOGO/HEADER
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(painter = painterResource(id = R.drawable.reykunyu_logo)
                            , contentDescription = null)
                        /*Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )*/
                    }

                    Spacer(Modifier.height(30.dp))
                    appViewModel.destinations.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.second, contentDescription = stringResource(item.third)) },
                            label = { Text(stringResource(item.third)) },
                            selected = item.first == appViewModel.screenState,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    appViewModel.navDrawerVisibility = DrawerValue.Closed
                                }
                                appViewModel.changeScreen(item.first)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        )
        {

            // Note: not using Navigation here because we want to reserve the back gesture for the
            // search feature. A custom back stack will be implemented to show previous searches
            AnimatedContent(targetState = appViewModel.screenState) {

                when (it) {
                    ScreenState.Search ->
                        DictionaryScreen(
                            offlineDictViewModel,
                            searchViewModel,
                            openNavDrawerAction = {
                                appViewModel.navDrawerVisibility = DrawerValue.Open
                                scope.launch { drawerState.open() }
                            }
                        )

                    ScreenState.Settings -> {
                        SettingsScreen(
                            preferenceViewModel = preferenceViewModel,
                            openNavDrawerAction = {
                                appViewModel.navDrawerVisibility = DrawerValue.Open
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }

            }

        }
    }
}