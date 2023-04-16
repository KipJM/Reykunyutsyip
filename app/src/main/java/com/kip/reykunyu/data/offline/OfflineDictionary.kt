package com.kip.reykunyu.data.offline

import android.content.Context
import android.util.Log
import com.kip.reykunyu.data.api.Response
import com.kip.reykunyu.data.api.ResponseStatus
import com.kip.reykunyu.data.api.ReykunyuApi
import com.kip.reykunyu.data.dict.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*

data class NaviDictionary (
    val dictionary: Map<String, DictNavi>,
    val indexedNavi: List<DictNavi> = dictionary.values.toList(),
    var indexedTranslations: List<Pair<String, Int>> = listOf()
    ) {

    //Update indexed translations based on language
    fun updateLang(language: Language) {
        indexedTranslations = mutableListOf()
        indexedNavi.forEachIndexed{ index, navi ->
            for (translation in navi.translations) {
                if (translation[language] != null) {
                    indexedTranslations +=
                        Pair(translation[language]!!, index)
                }
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
object OfflineDictionary {
    private const val dictCacheFilename = "DictionaryCache"
    private val expiration = Period.of(0, 0, 30)

    var dictionary: NaviDictionary? = null
        private set
    private val emptyDict: NaviDictionary = NaviDictionary(emptyMap(), emptyList(), emptyList())

    /**
     * A null-safe access for the dictionary. Returns an empty dictionary if not loaded. READ ONLY
     */
    val safeDictionary: NaviDictionary
        get() = dictionary ?: emptyDict




    suspend fun get(context: Context): Response<NaviDictionary> {
        val dictJson: String

        // Load dictionary from disk
        val diskResponse = tryRetrieveFromDisk(context, expiration)
        if (diskResponse.status == ResponseStatus.Success)
        {
            dictJson = diskResponse.content!!
        }
        else {
            // Download dictionary
            try
            {

                dictJson = ReykunyuApi.getDictionary()
                Log.i("REYKUNYU", "DICTIONARY DOWNLOADED!")
            }
            catch (e: Exception)
            {
                Log.e("REYKUNYU", e.toString())
                return Response(ResponseStatus.Error, message = "Download error ($e)")
            }
        }



        //Convert the dictionary json into internal data structure
        return if (
            convertDictionary(json = dictJson) // parse dictionary
        ) {
            Log.i("REYKUNYU", "DICTIONARY LOADED!")

            //Save dictionary IF ONLY THE DICTIONARY IS DOWNLOADED. Ignore if it comes from disk
            if (diskResponse.status != ResponseStatus.Success) {
                val saveResponse = saveToDisk(json = dictJson, context = context)

                if (saveResponse.status == ResponseStatus.Error) {
                    return Response(ResponseStatus.Error, message = saveResponse.message)
                }
            }

            Response(ResponseStatus.Success, dictionary)
        }
        else
        {
            Response(ResponseStatus.Error, message = "Loading error")
        }

    }


    private fun tryRetrieveFromDisk(context: Context, expiration: Period): Response<String> {

        //Find dictionary cache files
        val dictionaryFile = context.cacheDir.listFiles()
            ?.firstOrNull { it.name.contains(dictCacheFilename) }

        //Not found? Fallback to download from Internet.
        @Suppress("FoldInitializerAndIfToElvis")
        if (dictionaryFile == null) {
            Log.i("REYKUNYU", "HMM")
            return Response(ResponseStatus.Error)
        }

        val createdDate = Instant.ofEpochMilli(dictionaryFile.lastModified())
            .atZone(ZoneId.systemDefault()).toLocalDate()

        //The dictionary has expired. Download new one!
        if (createdDate.plus(expiration) < LocalDate.now()) { //Somehow this works??? I hav eno idea
            Log.i("REYKUNYU", "EXPIRATION DATE: ${createdDate.plus(expiration)}")
            Log.i("REYKUNYU", "CURRENT DATE: ${LocalDate.now()}")
            Log.i("REYKUNYU", "DIFF: ${createdDate.plus(expiration).compareTo(LocalDate.now())}")
            Log.i("REYKUNYU", "UHH")
            return Response(ResponseStatus.Error)
        }

        //Finally, we can read the file
        return try {
            Log.i("REYKUNYU", "YESSS")
            Response(ResponseStatus.Success, dictionaryFile.readText())
        } catch (exception: IOException) {
            Log.e("REYKUNYU", exception.message ?: "")
            Response(ResponseStatus.Error,
                message = "Unable to read the dictionary cache file! (${exception.message})")
        }



    }


    private suspend fun saveToDisk(json: String, context: Context): Response<Nothing> {
        try {

            // Android appends weird numbers in the filenames for cache files.
            // Delete the old ones as they will be replaced with the new one.
            // If it didn't work? Too bad. Redownload again or something
            context.cacheDir.listFiles()
                ?.filter { it.name.contains(dictCacheFilename) }
                ?.forEach { it.delete() }


            val file = withContext(Dispatchers.IO) {
                return@withContext File.createTempFile(dictCacheFilename, ".json")
            }
            file.writeText(json)

            return Response(ResponseStatus.Success)
        }
        catch (exception: IOException) {
            Log.e("REYKUNYU", exception.message ?: "")
            return Response(
                ResponseStatus.Error,
                message = "Error when trying to save dictionary to cache. (${exception.message})"
            )
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
        //Defaults to english, it will get auto-recomputed when searching with a different language
        dictionary?.updateLang(Language.English)

        return true
    }

}
