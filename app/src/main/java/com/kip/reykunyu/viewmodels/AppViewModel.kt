package com.kip.reykunyu.viewmodels

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.kip.reykunyu.R


sealed interface ScreenState {
    object Search : ScreenState
    object Settings: ScreenState
}

//Handles general app stuff
class AppViewModel: ViewModel() {

    var screenState: ScreenState by mutableStateOf(ScreenState.Search)
        private set

    var navDrawerVisibility by mutableStateOf(DrawerValue.Closed)

    fun toggleNavBar() {
        navDrawerVisibility =
            if (navDrawerVisibility == DrawerValue.Closed)
            {
                DrawerValue.Open
            } else {
                DrawerValue.Closed
            }
    }

    fun changeScreen(target: ScreenState) {
        if (screenState != target) {
            //Cleanup event or smthing here in the future
            screenState = target
        }
    }

    val destinations = listOf<Triple<ScreenState, ImageVector, @StringRes Int>>(
        Triple(ScreenState.Search, Icons.Filled.Search, R.string.search),
        Triple(ScreenState.Settings, Icons.Filled.Settings, R.string.settings)
    )

}