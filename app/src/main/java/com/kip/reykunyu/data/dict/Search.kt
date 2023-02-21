package com.kip.reykunyu.data.dict

import com.kip.reykunyu.data.offline.OfflineTranslateSearch

enum class SearchResultStatus{
    Standby,
    Loading,
    Success,
    Error
}

data class TranslateSearchResult(
    val status: SearchResultStatus,
    val fromNavi: List<Navi>, //Sort by relevance
    val toNavi: List<Navi> //Sort by relevance
)

interface TranslateSearchProvider{
    suspend fun search(query: String, language: Language = Language.English): TranslateSearchResult
}

interface DictionarySearchRepository {
    suspend fun search(query: String): TranslateSearchResult
}

object UniversalSearchRepository: DictionarySearchRepository {
    val offlineMode: Boolean = true
    val language: Language = Language.English

    private val offlineTranslateSearchProvider:TranslateSearchProvider = OfflineTranslateSearch()
//    private val onlineTranslateSearchProvider: TranslateSearchProvider = TODO()

    override suspend fun search(query: String): TranslateSearchResult {
        if (query.isBlank()) {
            return TranslateSearchResult(
                status = SearchResultStatus.Standby, emptyList(), emptyList())
        }

        return when (offlineMode) {
            true -> {
                //Offline
                offlineTranslateSearchProvider.search(query, language)
            }
            false -> {
                //Online
                //onlineTranslateSearchProvider.search(query, language)
                //TODO
                return TranslateSearchResult(status = SearchResultStatus.Error,
                    emptyList(), emptyList())
            }
        }
    }

}