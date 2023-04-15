package com.kip.reykunyu.data.online

import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.SearchResultStatus
import com.kip.reykunyu.data.dict.TranslateResult
import com.kip.reykunyu.data.dict.TranslateSearchProvider

class OnlineTranslateSearch : TranslateSearchProvider{
    override suspend fun search(query: String, language: Language): TranslateResult {
        try{
            val searchJson = ReykunyuApi.search(query, language)
        } catch(e: Exception) {
            return TranslateResult(SearchResultStatus.Error, emptyList(), emptyList(), info = "")
        }
        TODO()
    }
}



