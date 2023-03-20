package com.kip.reykunyu.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kip.reykunyu.data.dict.SearchResultStatus
import com.kip.reykunyu.data.dict.SearchType
import com.kip.reykunyu.data.dict.TranslateSearchResult
import com.kip.reykunyu.data.dict.UniversalSearchRepository
import kotlinx.coroutines.launch

sealed interface DictSearchState {
    object Standby : DictSearchState
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

    var searchType by mutableStateOf(SearchType.Translate) //TODO: Currently useless :/
        private set

    var offlineMode by mutableStateOf(false)
        private set


    fun updateSearchInput(input: String) {
        searchInput = input
    }


    fun updateSearchType(type: SearchType) {
        searchType = type
    }


    fun search() {
        dictSearchState = DictSearchState.Loading
        viewModelScope.launch {
            val response = UniversalSearchRepository.search(searchInput)
            dictSearchState = when (response.status) {
                SearchResultStatus.Success -> {
                    DictSearchState.Success(response)
                }

                SearchResultStatus.Error -> {
                    DictSearchState.Error
                }
                SearchResultStatus.Standby -> {
                    DictSearchState.Standby
                }
                SearchResultStatus.Loading -> {
                    DictSearchState.Standby
                }
            }
        }
    }
}