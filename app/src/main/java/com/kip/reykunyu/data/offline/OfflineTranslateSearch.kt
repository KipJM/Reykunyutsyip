package com.kip.reykunyu.data.offline

import com.kip.reykunyu.data.dict.*
import me.xdrop.fuzzywuzzy.FuzzySearch


const val relevanceLimit = 70
class OfflineTranslateSearch : TranslateSearchProvider {


    override suspend fun search(query: String, language: Language): TranslateSearchResult {
        if (OfflineDictionary.dictionary == null) {
            //Cancels search if dictionary is not here. This (hopefully) should be unreachable code!
            return TranslateSearchResult(SearchResultStatus.Error, emptyList(), emptyList())
        }
        val dictionary = OfflineDictionary.safeDictionary.dictionary
        val indexKeys = dictionary.keys.toList()

        // Na'vi=> search
        val fromNaviSearch = FuzzySearch.extractSorted(query,
            dictionary.values.map { it.word },
            relevanceLimit).toList()
        val fromNaviResults = fromNaviSearch.map { dictionary.filterValues }

        // =>Na'vi search
        val toNaviResults = OfflineDictionary.safeDictionary
            .dictionary.filterValues {translationsSimilarity(it, query, language)}.values.toList()

        return TranslateSearchResult(SearchResultStatus.Success, fromNaviResults, toNaviResults)
    }

}