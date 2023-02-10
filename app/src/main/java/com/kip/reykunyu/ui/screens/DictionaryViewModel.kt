package com.kip.reykunyu.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.offline.OfflineDictionary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoadingState {
    Standby,
    Loading,
    Success,
    Error
}

data class DictionaryUiState(
    val dictLoadingState: LoadingState = LoadingState.Standby,
    val debugJsonString: String = ""
)


class DictionaryViewModel: ViewModel() {
    //Screen UI state
    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()


    public fun downloadDictionary() {
        _uiState.update { it.copy(dictLoadingState = LoadingState.Loading) }
        viewModelScope.launch {
            val response = OfflineDictionary.download()

            _uiState.update {
                when(response.status){
                    ResponseStatus.Error -> it.copy(dictLoadingState = LoadingState.Error)
                    ResponseStatus.Success -> it.copy(
                        dictLoadingState = LoadingState.Success,
                        debugJsonString = "Dictionary loaded. Total of " +
                                "${response.content?.dictionary?.size} entries. \n" +
                                "Example word: skxawng: translation: " +
                                OfflineDictionary.dictionary!!.dictionary
                                        ["skxawng:n"]!!.translations[0][Language.English]
                    )
                }
            }


        }
    }


}