package com.kip.reykunyu.data.offline

import android.util.Log
import com.kip.reykunyu.data.api.Response
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.NaviIntermediate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.Dictionary
import kotlin.system.measureTimeMillis

data class NaviDictionary (
    val dictionary: Map<String, Navi>
    )

object OfflineDictionary {
    var dictionaryLoaded: Boolean = false
    var dictionary: NaviDictionary? = null


    private val jsonFormat = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    public suspend fun download(): Response<NaviDictionary> {
        try {
            var dictJson = ReykunyuApi.getDictionary()
            if(convertDictionary(json = dictJson)) {
                dictionaryLoaded = true
                return Response(ResponseStatus.Success, dictionary)
            } else {
                return Response(ResponseStatus.Error)
            }
        } catch (e: Exception) {
            Log.wtf("REYKUNYU", e)
            return Response(ResponseStatus.Error)
        }
    }

    /**
     * Convert Json to the Na'vi dictionary and saves it. Returns true if conversion succeeded.
     */
    public suspend fun convertDictionary(json: String): Boolean {
        //Time it!
        val time = measureTimeMillis {
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

            dictionary = NaviDictionary(naviDict.toMap())
        }
        Log.i("REYKUNYU", "Parsed the json and created the Na'vi dictionary in $time ms!")
        return true
    }

}
