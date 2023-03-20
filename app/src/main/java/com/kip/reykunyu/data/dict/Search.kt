package com.kip.reykunyu.data.dict

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kip.reykunyu.R
import com.kip.reykunyu.data.offline.OfflineTranslateSearch
import com.kip.reykunyu.data.online.OnlineTranslateSearch

enum class SearchResultStatus{
    Standby,
    Loading,
    Success,
    Error
}

data class TranslateSearchResult(
    val status: SearchResultStatus,
    val fromNavi: List<Pair<String,List<Navi>>>, //Split a sentence into words, translate indivisually
    val toNavi: List<Navi>, //Sort by relevance
    val info: String? = null
)

enum class SearchType(@DrawableRes val icon: Int, @StringRes val display: Int){
    Translate(R.drawable.translate_24, R.string.translate), //Na'vi <-> Language
    Sentence(R.drawable.sentence_24, R.string.sentence_analysis),  //Sentence Analysis
    Annotated(R.drawable.annotated_24, R.string.annotated), //Annotated Dictionary
    Rhymes(R.drawable.rhymes_24, R.string.rhymes),    //Rhymes
    Offline(R.drawable.offline_24, R.string.offline)    //Offline Cached Dictionary
}


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
    private val onlineTranslateSearchProvider: TranslateSearchProvider = OnlineTranslateSearch()

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
                onlineTranslateSearchProvider.search(query, language)
            }
        }
    }

}