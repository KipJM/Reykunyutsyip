package com.kip.reykunyu.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class OfflineDictionaryViewModel: ViewModel() {

    var offlineDictState: OfflineDictState by mutableStateOf(OfflineDictState.NotLoaded)
        private set

    fun downloadDictionary() {
        offlineDictState = OfflineDictState.Loading
        viewModelScope.launch {
            val response = OfflineDictionary.download()
            offlineDictState =
                when(response.status) {
                    ResponseStatus.Error -> OfflineDictState.Error
                    ResponseStatus.Success -> OfflineDictState.Loaded
                }
        }
    }
}