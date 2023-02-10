package com.kip.reykunyu.data.dict

import androidx.annotation.StringRes
import com.kip.reykunyu.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//fwew search works both ways. Cool!
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
    Navi(R.string.navi, "x-navi");

    override fun toString(): String {
        return requestCode
    }

    companion object {
        private val map = Language.values().associateBy(Language::requestCode)
        fun fromCode(requestCode: String) = map[requestCode]
    }

}


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




@Serializable
data class Audio(
    val speaker: String,
    val file: String
)
@Serializable
data class Pronunciation(
    val syllables: String,
    val stressed: Int?,
    val audio: List<Audio>?
)

//We convert Json to a data class first, then we clean it up
@Serializable
data class NaviIntermediate(
    @SerialName("na'vi")
    val navi: String,
    @SerialName("type")
    val wordType: String,

    val translations: List<Map<String, String>>,
    val pronunciation: List<Pronunciation>? = null,

    val infixes: String? = null,
    val meaning_note: String? = null,
    val etymology: String? = null,

    val seeAlso: List<String>? = null,
    val source: List<List<String>>? = null,

    val status: String? = null,
    val status_note: String? = null,

    val image: String? = null
)



@Serializable
data class Navi(
    val word: String,
    val type: String,

    val translations: List<Map<Language, String>>,
    val pronunciation: List<Pronunciation>?,

    val infixes: String?,
    val meaning_note: String?,
    val etymology: String?,

    val seeAlso: List<String>?,
    val source: List<List<String>>?,

    val status: String?,
    val status_note: String?,

    val image: String?
) {

    fun typeDisplay(): String {
        return typeMap[type] ?: "?"
    }
    fun typeDetails(): Int {
        return typeInfoMap[type] ?: R.string.unknown
    }


    companion object {
        fun create(intermediate: NaviIntermediate): Navi {

            //intransitive verbs don't have si appended for some reason, manually append them here
            var word = intermediate.navi
            if (intermediate.wordType == "n:si") {
                word += " si"
            }

            //Translations
            var translations = mutableListOf<Map<Language, String>>()
            for (translationColumn in intermediate.translations) {
                var column = mutableMapOf<Language, String>()
                for (translationItem in translationColumn) {
                    column[
                            Language.fromCode(translationItem.key) ?: Language.English
                    ] = translationItem.value
                }
                translations.add(column.toMap())
            }


            return Navi(
                word = word,
                type = intermediate.wordType,
                translations = translations.toList(),
                pronunciation = intermediate.pronunciation,
                infixes = intermediate.infixes,
                meaning_note = intermediate.meaning_note,
                etymology = intermediate.etymology,
                seeAlso = intermediate.seeAlso,
                source = intermediate.source,
                status = intermediate.status,
                status_note = intermediate.status_note,
                image = intermediate.image
            )
        }

    }
}

