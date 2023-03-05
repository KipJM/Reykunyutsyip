package com.kip.reykunyu.data.offline

import android.util.Log
import com.kip.reykunyu.data.api.Response
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.UniversalSearchRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString

data class NaviDictionary (
    val dictionary: Map<String, DictNavi>,
    val indexedNavi: List<DictNavi> = dictionary.values.toList(),
    var indexedTranslations: List<Pair<String, Int>> = listOf()
    ) {

    //Update indexed translations based on language
    fun updateLang() {
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




    suspend fun get(): Response<NaviDictionary> {
        val dictJson: String
        try //Download dictionary
        {
            dictJson = ReykunyuApi.getDictionary()
            Log.i("REYKUNYU", "DICTIONARY DOWNLOADED!")
        }
        catch (e: Exception)
        {
            Log.wtf("REYKUNYU", e)
            return Response(ResponseStatus.Error, message = "Download error ($e)")
        }

        return if (
            convertDictionary(json = dictJson) // parse dictionary
        ) {
            Log.i("REYKUNYU", "DICTIONARY LOADED!")
            Response(ResponseStatus.Success, dictionary)
        }
        else
        {
            Response(ResponseStatus.Error, message = "Loading error")
        }
    }


    /**
     * Convert Json to the Na'vi dictionary and saves it. Returns true if conversion succeeded.
     */
    private fun convertDictionary(json: String): Boolean {
        val naviDict = mutableMapOf<String, DictNavi>()
        try {

            //Parse
            val dictRaw =
                ReykunyuApi.jsonFormat.decodeFromString<Map<String, DictNaviRaw>>(json)

            //Convert raw Navi to DictNavi
            for (naviRaw in dictRaw) {
                naviDict[naviRaw.key] = naviRaw.value.toDictNavi()
            }

        }
        catch (e: Exception)
        {
            Log.wtf("REYKUNYU", e)
            return false
        }

        //Save dictionary to class, and also
        dictionary = NaviDictionary(naviDict.toMap(), indexedTranslations = mutableListOf())
        dictionary?.updateLang()

        return true
    }

}
