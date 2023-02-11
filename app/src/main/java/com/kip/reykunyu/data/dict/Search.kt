package com.kip.reykunyu.data.dict

import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.offline.OfflineTranslateSearch

data class TranslateSearchResult(
    val status: ResponseStatus,
    val fromNavi: List<Navi>,
    val toNavi: List<Navi>
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
        if (query == "") {
            return TranslateSearchResult(status = ResponseStatus.Success, emptyList(), emptyList())
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
                return TranslateSearchResult(status = ResponseStatus.Error, emptyList(), emptyList())
            }
        }
    }

}