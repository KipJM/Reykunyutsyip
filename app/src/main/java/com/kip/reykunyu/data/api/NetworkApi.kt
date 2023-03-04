package com.kip.reykunyu.data.api

import com.kip.reykunyu.data.dict.Language
import kotlinx.serialization.ExperimentalSerializationApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

internal const val BASE_URL =
    "https://reykunyu.lu/api/"

private const val DICT_URL =
    "list/all"

@Deprecated("SEARCH is better")
private const val LANG_LOOKUP_DEPRECATED =
    "search?query={QUERY}&language={LANG}"

private const val SEARCH =
    "fwew-search?query={QUERY}&language={LANG}"


private const val LANG_SUGGEST =
    "suggest?query={QUERY}&language={LANG}"

private const val NAVI_SUGGEST =
    "mok?tìpawm"


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
    suspend fun search(@Path("QUERY") query: String, @Path("LANG") language: String): String

    @GET(LANG_SUGGEST)
    suspend fun suggestLang(@Path("QUERY") query: String, @Path("LANG") language: String): String

    @GET(NAVI_SUGGEST)
    suspend fun suggestNavi(@Path("QUERY") query: String, @Path("LANG") language: String): String

}

object ReykunyuApi {

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

    suspend fun search(query: String, language: Language): String {
        return retrofitService.search(query, language.toString())
    }
}