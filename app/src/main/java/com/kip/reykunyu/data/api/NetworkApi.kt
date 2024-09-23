package com.kip.reykunyu.data.api

import com.kip.reykunyu.data.dict.Dialect
import com.kip.reykunyu.data.dict.Language
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

internal const val BASE_URL =
    "https://reykunyu.lu/api/"

private const val DICT_URL =
    "list/all"

@Deprecated("SEARCH is better")
private const val LANG_LOOKUP_DEPRECATED =
    "search"


//fwew-search works both ways. Cool!
private const val SEARCH =
    "fwew-search"


private const val LANG_SUGGEST =
    "suggest"

private const val NAVI_SUGGEST =
    "mok"


data class Response<T>(
    val status: ResponseStatus,
    val content: T? = null,
    val message: String? = null
)

enum class ResponseStatus{
    Error,
    Success
}


interface ReykunyuApiService {
    //Json parsing and conversion is handled outside of the API service. Returns raw json

    @GET(DICT_URL)
    suspend fun getDictionary(): String

    @GET(SEARCH)
    suspend fun search(@Query("query") query: String, @Query("language") language: String, @Query("dialect") dialect: String): String

    @GET(LANG_SUGGEST)
    suspend fun suggestLang(@Query("query") query: String, @Query("language") language: String, @Query("dialect") dialect: String): String

    @GET(NAVI_SUGGEST)
    suspend fun suggestNavi(@Query("tìpawm") query: String, @Query("language") language: String, @Query("dialect") dialect: String): String

}

object ReykunyuApi {
    @OptIn(ExperimentalSerializationApi::class)
    val jsonFormat = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }


    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService : ReykunyuApiService by lazy {
        retrofit.create(ReykunyuApiService::class.java)
    }

    suspend fun getDictionary(): String {
        return retrofitService.getDictionary()
    }

    suspend fun search(query: String, language: Language, dialect: Dialect): String {
        return retrofitService.search(query, language.toString(), dialect.toString())
    }

    suspend fun getSuggestionsLang(query: String, language: Language, dialect: Dialect): String {
        return retrofitService.suggestLang(query, language.toString(), dialect.toString())
    }

    suspend fun getSuggestionsNavi(query: String, language: Language, dialect: Dialect): String {
        return retrofitService.suggestNavi(query, language.toString(), dialect.toString())
    }
}