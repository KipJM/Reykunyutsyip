package com.kip.reykunyu.data.dict

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import com.kip.reykunyu.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


enum class Language(@StringRes val display: Int, private val requestCode: String) {
    German(R.string.german, "de"),
    Estonian(R.string.estonian, "et"),
    English(R.string.english, "en"),
    French(R.string.french, "fr"),
    Hungarian(R.string.hungarian, "hu"),
    Dutch(R.string.dutch, "nl"),
    Polish(R.string.polish, "pl"),
    Russian(R.string.russian, "ru"),
    Swedish(R.string.swedish, "sv"),
    Navi(R.string.navi, "x-navi"),
    Unknown(R.string.lang_unknown, "");

    override fun toString(): String {
        return requestCode
    }

    companion object {
        private val map = Language.values().associateBy(Language::requestCode)
        fun fromCode(requestCode: String) = map[requestCode]
    }

}

//region type conversion
val typeMap: Map<String, String> = mapOf(
    "n" to "n.",
    "n:unc" to "n.",
    "n:si" to "vin.",
    "n:pr" to "prop. n.",
    "pn" to "pn.",
    "adj" to "adj.",
    "num" to "num.",
    "adv" to "adv.",
    "adp" to "adp.",
    "adp:len" to "adp+",
    "intj" to "intj.",
    "part" to "part.",
    "conj" to "conj.",
    "ctr" to "sbd.",
    "v:?" to "v.",
    "v:in" to "vin.",
    "v:tr" to "vtr.",
    "v:m" to "vm.",
    "v:si" to "v.",
    "v:cp" to "vcp.",
    "phr" to "phr.",
    "inter" to "inter.",
    "aff:pre" to "pref.",
    "aff:in" to "inf.",
    "aff:suf" to "suf.",
    "nv:si" to "vin.",
    "?" to "?"
)

//StringRes
val typeInfoMap: Map<String, Int> = mapOf(
    "n" to R.string.n,
    "n:unc" to R.string.n,
    "n:si" to R.string.n_si,
    "n:pr" to R.string.n_pr,
    "pn" to R.string.pn,
    "adj" to R.string.adj,
    "num" to R.string.num,
    "adv" to R.string.adv,
    "adp" to R.string.adp,
    "adp:len" to R.string.adp_len,
    "intj" to R.string.intj,
    "part" to R.string.part,
    "conj" to R.string.conj,
    "ctr" to R.string.ctr,
    "v:?" to R.string.v_unknown,
    "v:in" to R.string.v_in,
    "v:tr" to R.string.v_tr,
    "v:m" to R.string.v_m,
    "v:si" to R.string.v_si,
    "v:cp" to R.string.v_cp,
    "phr" to R.string.phr,
    "inter" to R.string.inter,
    "aff:pre" to R.string.aff_pre,
    "aff:in" to R.string.aff_in,
    "aff:suf" to R.string.aff_suf,
    "nv:si" to R.string.nv_si,
    "?" to R.string.unknown
)
//endregion



@Serializable
data class Pronunciation(
    val syllables: String,
    val stressed: Int?,
    val audio: List<Audio>?,
    val ipa: Ipa? = null //TODO: Currently unused
) {
    @Serializable
    data class Ipa(
        @SerialName("FN")
        val fn: String?,
        @SerialName("RN")
        val rn: String?
    )
}

@Serializable
data class Audio(
    val speaker: String,
    val file: String
)


data class Source(
    val type: Type,
    val richUrl: RichText? = null,
    val richText: List<RichText>? = null
) {
    enum class Type{
        RichURL,
        RichText
    }

    companion object {
        fun createList(raw: List<List<String>>?): List<Source>? {
            if (raw.isNullOrEmpty()) {
                return null
            }

            val cleanRaw = mutableListOf<List<String>>()

            //Remove random empty elements (List cleanup)
            for (source in raw) {
                val sourceClean = source.filter{ o -> o.isNotBlank() }

                if (sourceClean.isNotEmpty()) {
                    cleanRaw += sourceClean
                }
            }
            if(cleanRaw.isEmpty()) {
                return null
            }

            val sources = mutableListOf<Source>()

            for (source in cleanRaw) {
                //Two elements: Rich URL!
                // otherwise URL and text can be automatically handled by RichText
                if (source.size == 2 &&
                    !Patterns.WEB_URL.matcher(source[0]).matches() &&
                    Patterns.WEB_URL.matcher(source[1]).matches()
                ) {
                    sources.add(Source(
                        Type.RichURL,
                        RichText(listOf(RichText.Partition(
                            RichText.Partition.Type.Url,
                            urlDisplay = AnnotatedString(source[0]),
                            url = source[1]
                        )))
                    ))
                }
                else{
                    sources.add(Source(
                        Type.RichText,
                        richText = source.map{ o -> RichText.create(o) ?: RichText(emptyList()) }
                    ))
                }
            }

            return sources
        }
    }
}


data class Navi(
    val word: String,
    val type: String,

    val translations: List<Map<Language, String>>,
    val pronunciation: List<Pronunciation>?,

    val conjugatedExplanation: List<ConjugatedExplanation>?,

    val infixes: String?,

    val meaning_note: List<RichText>?,
    val etymology: RichText?,

    val seeAlso: List<String>?,
    val derived: List<String>?,

    val image: String?,

    val status: String?,
    val status_note: RichText?,

    val source: List<Source>?,
) {

    fun typeDisplay(): String {
        return typeMap[type] ?: "?"
    }
    fun typeDetails(): Int {
        return typeInfoMap[type] ?: R.string.unknown
    }

}

