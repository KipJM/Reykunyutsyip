package com.kip.reykunyu.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.SearchMode
import com.kip.reykunyu.data.dict.SearchResultStatus
import com.kip.reykunyu.data.dict.SuggestionsResult
import com.kip.reykunyu.data.dict.SuggestionsStatus
import com.kip.reykunyu.data.dict.TranslateResult
import com.kip.reykunyu.data.dict.UniversalSearchRepository
import com.kip.reykunyu.data.dict.UniversalSuggestionsRepository
import kotlinx.coroutines.launch

sealed interface SearchState {
    object Standby : SearchState
    object Loading : SearchState

    data class TranslateSuccess(val result: TranslateResult) : SearchState
    data class SentenceSuccess(val result: TranslateResult) : SearchState //TODO: Proper results
    data class RhymesSuccess(val result: TranslateResult) : SearchState //TODO: Proper results
    data class AnnotatedSuccess(val result: TranslateResult) : SearchState //TODO: Proper results


    data class Error(val info: String?, val id: String? = null) : SearchState
}

//Handles all the search stuff
class DictionarySearchViewModel: ViewModel() {

    var searchState: SearchState by mutableStateOf(SearchState.Standby)
        private set

    var searchInput by mutableStateOf("")
        private set

    var searchSuggestions by mutableStateOf(SuggestionsResult())
        private set

    var searchMode by mutableStateOf(SearchMode.Translate) //TODO: Currently useless :/
        private set

    //Determines if Reykunyu is reachable
    var offlineMode by mutableStateOf(false)
        private set


    fun updateSearchInput(input: String) {
        searchInput = input
    }

    fun updateSuggestions(language: Language) {
        if(searchInput.count() >= 3){
            //Start suggesting!
            searchSuggestions = SuggestionsResult(SuggestionsStatus.Loading)
            viewModelScope.launch {
                searchSuggestions = UniversalSuggestionsRepository.suggest(
                    query = searchInput, language = language, mode = searchMode)
            }
        }else{
            searchSuggestions = SuggestionsResult(SuggestionsStatus.Standby)
        }
    }

    fun updateSearchType(type: SearchMode) {
        searchMode = type
    }


    fun search(language: Language) {
        searchState = SearchState.Loading
        viewModelScope.launch {
            when (searchMode) {
                SearchMode.Translate -> translate(true, language)
                SearchMode.Sentence -> sentenceAnalysis()
                SearchMode.Annotated -> annotatedDictionarySearch()
                SearchMode.Rhymes -> rhymesSearch()
                SearchMode.Offline -> translate(false, language)
            }
        }
    }

    private suspend fun sentenceAnalysis() {
        comingSoonNotice()
    }

    private suspend fun annotatedDictionarySearch() {
        comingSoonNotice()
    }

    private suspend fun rhymesSearch() {
        comingSoonNotice()
    }

    private fun comingSoonNotice() {
        searchState = SearchState.Error("Coming soon!", "COMING_SOON")
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