package com.kip.reykunyu.data.dict

import android.util.Log
import com.kip.reykunyu.R
import com.kip.reykunyu.data.api.ReykunyuApi
import kotlinx.serialization.Serializable


@Serializable
data class NaviSuggestionRaw(
    val title: String,
    val description: String? = null
){
    fun convert(): NaviSuggestion {
        val title = title
            .replace("<span class=\"stressed\">", "")
            .replace("</span>", "")
        val type = description
            ?.substringAfter("<div class=\"ui horizontal label\">")
            ?.substringBefore("</div> ")
        val explanation = description?.substringAfter("</div>")

        return NaviSuggestion(
            word = title,
            type = type,
            explanation = explanation
        )
    }
}

@Serializable
data class NaviSuggestionsRaw(
    val results: List<NaviSuggestionRaw>
)




data class NaviSuggestion(
    val word: String,
    val type: String? = null,
    val explanation: String? = null
){
    fun typeDetails(): Int {
        return typeInfoMap[typeMap.entries.find { it.value == type }?.key] ?: R.string.unknown
    }
}

enum class SuggestionsStatus{
    Standby,
    Loading,
    Success,
    Error
}

data class SuggestionsResult(
    val status: SuggestionsStatus = SuggestionsStatus.Standby,
    val fromNavi: List<NaviSuggestion>? = null,
    val toNavi: List<NaviSuggestion>? = null,
    val info: String? = null

)

interface SuggestionsProvider{
    suspend fun suggest(query: String, language: Language, dialect: Dialect): SuggestionsResult
}
interface SuggestionsRepository{
    suspend fun suggest(mode: SearchMode, query: String, language: Language, dialect: Dialect): SuggestionsResult
}

object UniversalSuggestionsRepository: SuggestionsRepository {
    override suspend fun suggest(
        mode: SearchMode,
        query: String,
        language: Language,
        dialect: Dialect
    ): SuggestionsResult {
        return when(mode){
            SearchMode.Translate -> TranslateSuggestionsProvider().suggest(query, language, dialect)
            SearchMode.Sentence -> SuggestionsResult(SuggestionsStatus.Error, info = "Coming soon!(TM)") //TODO
            SearchMode.Annotated -> SuggestionsResult(SuggestionsStatus.Error, info = "Coming soon!(TM)") //TODO
            SearchMode.Rhymes -> SuggestionsResult(SuggestionsStatus.Error, info = "Coming soon!(TM)") //TODO
            SearchMode.Offline -> SuggestionsResult(SuggestionsStatus.Error, info = "Suggestions are currently not available for offline dictionary.") //No suggestions for offline mode
        }
    }

}

class TranslateSuggestionsProvider: SuggestionsProvider{
    override suspend fun suggest(query: String, language: Language, dialect: Dialect): SuggestionsResult {
        try{
            return SuggestionsResult(
                SuggestionsStatus.Success,
                fromNavi = fromNaviSuggestions(query, language, dialect),
                toNavi = toNaviSuggestions(query, language)
            )
        }
        catch (e: Exception) {
            Log.e("REYKUNYU", e.toString())
            return SuggestionsResult(
                SuggestionsStatus.Error,
                fromNavi = null,
                toNavi = null,
                info = "error: Encountered an error while getting suggestions: $e"
            )
        }
    }

}


//Common funcs
suspend fun fromNaviSuggestions(query: String, language: Language, dialect: Dialect): List<NaviSuggestion>
{

    val suggestJson = ReykunyuApi.getSuggestionsNavi(query, language, dialect)
    return convertSuggestions(suggestJson)
}


suspend fun toNaviSuggestions(query: String, language: Language): List<NaviSuggestion>
{

    val suggestJson = "{results:[]}"//ReykunyuApi.getSuggestionsLang(query, language); //API Removed
    return convertSuggestions(suggestJson)
}

fun convertSuggestions(json: String): List<NaviSuggestion>
{
    //Convert to raw
    val suggestionsRaw =
        ReykunyuApi.jsonFormat.decodeFromString<NaviSuggestionsRaw>(json)

    return suggestionsRaw.results.map { it.convert() }
}
