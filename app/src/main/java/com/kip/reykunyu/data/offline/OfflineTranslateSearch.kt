package com.kip.reykunyu.data.offline

import com.kip.reykunyu.data.dict.*

class OfflineTranslateSearch : TranslateSearchProvider {

    override suspend fun search(query: String, language: Language): TranslateSearchResult {
        if (OfflineDictionary.dictionary == null) {
            //Cancels search if dictionary is not here. This (hopefully) should be unreachable code!
            return TranslateSearchResult(SearchResultStatus.Error, emptyList(), emptyList())
        }

        // Na'vi=> search
        val fromNaviResults = OfflineDictionary.safeDictionary
                .dictionary.filterValues { stringSimilarity(it.word, query) }.values.toList()

        // =>Na'vi search
        val toNaviResults = OfflineDictionary.safeDictionary
            .dictionary.filterValues {translationsSimilarity(it, query, language)}.values.toList()

        return TranslateSearchResult(SearchResultStatus.Success, fromNaviResults, toNaviResults)
    }

    private fun stringSimilarity(candidate: String, target: String): Boolean {
        return candidate.contains(target, ignoreCase = true)
    }

    private fun translationsSimilarity(candidate: Navi, target: String, language: Language
    ): Boolean {
        for (translationColumn in candidate.translations) {
            if (stringSimilarity(translationColumn[language]?: "", target))
                return true
        }
        return false
    }
}