package com.kip.reykunyu.data.dict

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kip.reykunyu.R
import com.kip.reykunyu.data.offline.OfflineTranslateSearch
import com.kip.reykunyu.data.online.OnlineTranslateSearch

enum class SearchResultStatus{
    Standby,
    Success,
    Error
}

data class TranslateResult(
    val status: SearchResultStatus,
    val fromNavi: List<Pair<String,List<Navi>>>, //Split a sentence into words, translate indivisually
    val toNavi: List<Navi>, //Sort by relevance
    val info: String? = null
)

enum class SearchMode(@DrawableRes val icon: Int, @StringRes val display: Int){
    Translate(R.drawable.translate_24, R.string.translate), //Na'vi <-> Language
    Sentence(R.drawable.sentence_24, R.string.sentence_analysis),  //Sentence Analysis
    Annotated(R.drawable.annotated_24, R.string.annotated), //Annotated Dictionary
    Rhymes(R.drawable.rhymes_24, R.string.rhymes),    //Rhymes
    Offline(R.drawable.offline_24, R.string.offline)    //Offline Cached Dictionary
}


interface TranslateSearchProvider{
    suspend fun search(query: String, language: Language, dialect: Dialect): TranslateResult
}

interface DictionarySearchRepository {
    suspend fun translate(query: String, online: Boolean, language: Language, dialect: Dialect): TranslateResult
}

object UniversalSearchRepository: DictionarySearchRepository {
    var previousLanguage: Language = Language.English

    private val offlineTranslateSearchProvider:TranslateSearchProvider = OfflineTranslateSearch()
    private val onlineTranslateSearchProvider: TranslateSearchProvider = OnlineTranslateSearch()

    override suspend fun translate(
        query: String,
        online: Boolean,
        language: Language,
        dialect: Dialect
    ): TranslateResult {

        if (query.isBlank()) {
            return TranslateResult(
                status = SearchResultStatus.Standby, emptyList(), emptyList())
        }

        return when (online) {
            false -> {
                //Offline
                val result = offlineTranslateSearchProvider.search(query, language, dialect)
                previousLanguage = language
                return result
            }
            true -> {
                //Online
                onlineTranslateSearchProvider.search(query, language, dialect)
            }
        }
    }

}