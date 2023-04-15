package com.kip.reykunyu.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kip.reykunyu.ReykunyuApplication
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.offline.OfflineDictionary
import kotlinx.coroutines.launch

sealed interface OfflineDictState {
    object NotLoaded : OfflineDictState
    object Loading : OfflineDictState
    object Loaded : OfflineDictState
    object Error : OfflineDictState
}


//Handles offline dictionary download
class OfflineDictionaryViewModel(
    private val application: Application
): ViewModel() {


    var offlineDictState: OfflineDictState by mutableStateOf(OfflineDictState.NotLoaded)
        private set

    fun downloadDictionary() {
        offlineDictState = OfflineDictState.Loading
        viewModelScope.launch {
            val response = OfflineDictionary.get(application.applicationContext)
            offlineDictState =
                when(response.status) {
                    ResponseStatus.Error -> OfflineDictState.Error
                    ResponseStatus.Success -> OfflineDictState.Loaded
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ReykunyuApplication)
                OfflineDictionaryViewModel(application)
            }
        }
    }
}