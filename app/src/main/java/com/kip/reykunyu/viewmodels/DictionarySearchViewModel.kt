package com.kip.reykunyu.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.dict.TranslateSearchResult
import com.kip.reykunyu.data.dict.UniversalSearchRepository
import kotlinx.coroutines.launch

sealed interface DictSearchState {
    object Standby : DictSearchState //Only used for initialization.
    object Loading : DictSearchState
    data class Success(val result: TranslateSearchResult) : DictSearchState
    object Error : DictSearchState
}


//Handles all the search stuff
class DictionarySearchViewModel: ViewModel() {

    var dictSearchState: DictSearchState by mutableStateOf(DictSearchState.Standby)
        private set

    var searchInput by mutableStateOf("")
        private set


    fun updateSearchInput(input: String) {
        searchInput = input
    }


    fun search() {
        dictSearchState = DictSearchState.Loading
        viewModelScope.launch {
            val response = UniversalSearchRepository.search(searchInput)
            dictSearchState = when (response.status) {
                ResponseStatus.Success -> {
                    DictSearchState.Success(response)
                }

                ResponseStatus.Error -> {
                    DictSearchState.Error
                }
            }
        }
    }
}