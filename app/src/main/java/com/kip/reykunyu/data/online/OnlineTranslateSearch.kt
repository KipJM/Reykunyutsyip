package com.kip.reykunyu.data.online

import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.SearchResultStatus
import com.kip.reykunyu.data.dict.TranslateSearchProvider
import com.kip.reykunyu.data.dict.TranslateSearchResult

class OnlineTranslateSearch : TranslateSearchProvider{
    override suspend fun search(query: String, language: Language): TranslateSearchResult {
        try{
            val dictJson = ReykunyuApi.getDictionary()
        } catch(e: Exception) {
            return TranslateSearchResult(SearchResultStatus.Error, emptyList(), emptyList())
        }
        TODO()
    }
}