package com.kip.reykunyu.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kip.reykunyu.data.dict.*
import kotlinx.coroutines.launch

sealed interface SearchState {
    object Standby : SearchState
    object Loading : SearchState

    data class TranslateSuccess(val result: TranslateResult) : SearchState
    data class SentenceSuccess(val result: TranslateResult) : SearchState //TODO: Proper results
    data class RhymesSuccess(val result: TranslateResult) : SearchState //TODO: Proper results
    data class AnnotatedSuccess(val result: TranslateResult) : SearchState //TODO: Proper results


    data class Error(val info: String?) : SearchState
}


//Handles all the search stuff
class DictionarySearchViewModel: ViewModel() {

    var searchState: SearchState by mutableStateOf(SearchState.Standby)
        private set

    var searchInput by mutableStateOf("")
        private set

    var searchMode by mutableStateOf(SearchMode.Translate) //TODO: Currently useless :/
        private set

    //Determines if Reykunyu is reachable
    var offlineMode by mutableStateOf(false)
        private set


    fun updateSearchInput(input: String) {
        searchInput = input
    }


    fun updateSearchType(type: SearchMode) {
        searchMode = type
    }


    fun search(language: Language) {
        searchState = SearchState.Loading
        viewModelScope.launch {
            when (searchMode) {
                SearchMode.Translate -> translate(true, language)
                SearchMode.Sentence -> TODO()
                SearchMode.Annotated -> TODO()
                SearchMode.Rhymes -> TODO()
                SearchMode.Offline -> translate(false, language)
            }
        }
    }

    private suspend fun translate(online: Boolean, language: Language) {
        val response = UniversalSearchRepository.translate(searchInput, online, language)

        searchState = when (response.status) {
            SearchResultStatus.Success -> {
                SearchState.TranslateSuccess(response)
            }

            SearchResultStatus.Error -> {
                SearchState.Error(response.info)
            }
            SearchResultStatus.Standby -> {
                SearchState.Standby
            }
        }
    }
}