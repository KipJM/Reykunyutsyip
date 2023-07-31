package com.kip.reykunyu.data.online

import android.util.Log
import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.*
import kotlinx.serialization.*

class OnlineTranslateSearch : TranslateSearchProvider{
    override suspend fun search(query: String, language: Language): TranslateResult {
        //Downloads data from Reykunyu's server
        val searchJson: String
        try{
            searchJson = ReykunyuApi.search(query, language)

        } catch(e: Exception) {
            Log.e("REYKUNYU", e.toString())
            return TranslateResult(SearchResultStatus.Error, emptyList(), emptyList(), info = "Encountered error when trying to communicate with Reykunyu")
        }
        //Log.i("REYKUNYU", searchJson)

        //Parses json to internal data structure
        return convertTranslationResult(searchJson)
    }


    fun convertTranslationResult(json: String): TranslateResult{
        try{
            //Parse json to serialized data
            val resultsRaw = ReykunyuApi.jsonFormat.decodeFromString<OnlineTranslateResult>(json)

            //from Na'vi extraction
            val fromNavi: MutableList<Pair<String, List<Navi>>> = mutableListOf()
            for (word in resultsRaw.fromNavi) {
                val naviList = word.result.map { it.toNavi() } //convert to universal Na'vi format
                fromNavi += Pair(word.query, naviList)
            }

            //to Na'vi extraction
            val toNavi: List<Navi> = resultsRaw.toNavi.map { it.toNavi() }

            return TranslateResult(SearchResultStatus.Success, fromNavi, toNavi)
        }
        catch (e: Exception) {
            Log.e("REYKUNYU", e.toString())
            return TranslateResult(SearchResultStatus.Error, emptyList(), emptyList())
        }

    }
}

@Serializable
data class OnlineTranslateResult(
    @SerialName("fromNa'vi")
    val fromNavi: List<FromNaviResults>,

    @SerialName("toNa'vi")
    val toNavi: List<OnlineNaviRaw>
)

//@OptIn(ExperimentalSerializationApi::class)
//@Serializer(forClass = OnlineTranslateResult::class)
//object OnlineTranslateResultSerializer : KSerializer<OnlineTranslateResult> {
//    override fun deserialize(decoder: Decoder): OnlineTranslateResult {
//        val json = (decoder as JsonDecoder).decodeJsonElement() as JsonObject
//        return
//    }
//
//    fun parseFromNavi(json: JsonObject): FromNaviResults {
//        val fromList = json["fromNa'vi"] ?: return FromNaviResults("", emptyList(), emptyList())
//
//        return Json.decodeFromString<FromNaviResults>(fromList.toString())
//    }
//}


@Serializable
data class FromNaviResults(
    @SerialName("tìpawm")
    val query: String,

    @SerialName("sì'eyng")
    val result: List<OnlineNaviRaw>,

    @SerialName("aysämok")
    val suggestions: List<String>
)
