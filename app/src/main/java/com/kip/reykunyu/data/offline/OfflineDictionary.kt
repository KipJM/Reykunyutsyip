package com.kip.reykunyu.data.offline

import android.util.Log
import com.kip.reykunyu.data.api.Response
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.NaviIntermediate
import com.kip.reykunyu.data.dict.UniversalSearchRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class NaviDictionary (
    val dictionary: Map<String, Navi>,
    val indexedNavi: List<Navi> = dictionary.values.toList(),
    var indexedTranslations: List<Pair<String, Int>> = listOf()
    ) {

    //Update indexed translations based on language
    fun update() {
        indexedTranslations = mutableListOf()
        indexedNavi.forEachIndexed{ index, navi ->
            for (translation in navi.translations) {
                if (translation[UniversalSearchRepository.language] != null) {
                    indexedTranslations +=
                        Pair(translation[UniversalSearchRepository.language]!!, index)
                }
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
object OfflineDictionary {
    var dictionary: NaviDictionary? = null
        private set
    private val emptyDict: NaviDictionary = NaviDictionary(emptyMap(), emptyList(), emptyList())

    /**
     * A null-safe access for the dictionary. Returns an empty dictionary if not loaded. READ ONLY
     */
    val safeDictionary: NaviDictionary
        get() = dictionary ?: emptyDict


    private val jsonFormat = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    suspend fun get(): Response<NaviDictionary> {
        return try {
            val dictJson = ReykunyuApi.getDictionary()
            Log.i("REYKUNYU", "DICTIONARY DOWNLOADED!")
            if(convertDictionary(json = dictJson)) {
                Log.i("REYKUNYU", "DICTIONARY LOADED!")
                Log.i("REYKUNYUFLOOD", "${dictionary?.indexedNavi?.size} words loaded!")
                Response(ResponseStatus.Success, dictionary)
            } else {
                Response(ResponseStatus.Error)
            }
        } catch (e: Exception) {
            Log.wtf("REYKUNYU", e)
            Response(ResponseStatus.Error)
        }
    }

    /**
     * Convert Json to the Na'vi dictionary and saves it. Returns true if conversion succeeded.
     */
    private fun convertDictionary(json: String): Boolean {
        val naviDict = mutableMapOf<String, Navi>()
        try {
            val dictIntermediate =
                jsonFormat.decodeFromString<Map<String, NaviIntermediate>>(json)
            for (intermediate in dictIntermediate) {
                naviDict[intermediate.key] = Navi.create(intermediate.value)
            }
        } catch (e: Exception) {
            Log.wtf("REYKUNYU", e)
            return false
        }

        dictionary = NaviDictionary(naviDict.toMap(), indexedTranslations = mutableListOf())
        dictionary?.update()


        return true
    }

}
