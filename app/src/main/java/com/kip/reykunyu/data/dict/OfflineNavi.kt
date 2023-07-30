package com.kip.reykunyu.data.offline

import com.kip.reykunyu.data.dict.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



// We convert Json to a data class first, then we clean it up
// "Raw" v1 Na'vi structure.
@Serializable
data class DictNaviRaw(
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
) {

    fun toDictNavi(): DictNavi {
        //intransitive verbs don't have si appended for some reason, manually append them here
        var word = navi
        if (wordType == "n:si") {
            word += " si"
        }

        //Translations
        val outputTranslation = mutableListOf<Map<Language, String>>()
        for (translationColumn in translations) {
            val column = mutableMapOf<Language, String>()
            for (translationItem in translationColumn) {
                column[
                        Language.fromCode(translationItem.key) ?:
                        // The dict Json uses multiple names for lang=na'vi,
                        // Such as "xNavi", "x-navi"...
                        if(translationItem.key.lowercase().contains("navi"))
                            Language.Navi else Language.Unknown
                ] = translationItem.value
            }
            outputTranslation.add(column.toMap())
        }


        return DictNavi(
            word = word,
            type = wordType,
            translations = outputTranslation.toList(),
            pronunciation = pronunciation,
            infixes = infixes,
            meaning_note = meaning_note,
            etymology = etymology,
            seeAlso = seeAlso,
            source = source,
            status = status,
            status_note = status_note,
            image = image
        )
    }
}


// v1 Na'vi structure. Will be parsed into Na'vi v2 (Reykunyu online search format) when needed.
// Main difference: Strings should be converted into RichText by parsing through regex
@Serializable
data class DictNavi(
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

/*    fun typeDisplay(): String {
        return typeMap[type] ?: "?"
    }
    fun typeDetails(): Int {
        return typeInfoMap[type] ?: R.string.unknown
    }*/

    fun toNavi(): Navi {

        val meaningNoteList = if (!meaning_note.isNullOrEmpty()) {
            listOf(meaning_note)
        }
        else{ null }

        return Navi(
            word = word,
            type = type,
            translations = translations,
            pronunciation = pronunciation,
            infixes = infixes,
            meaningNote = meaningNoteList?.mapNotNull { o -> RichText.create(o) },
            etymology = RichText.create(etymology),
            seeAlso = seeAlso?.map
            {
                // [skxawng:n] -> skxawng
                it.removePrefix("[").removeSuffix("]").
                split(':', ignoreCase = true)[0]
            },
            derived = null, //Only available for online
            image = image,
            status = status,
            statusNote = RichText.create(status_note),
            source = Source.createList(source),
            conjugatedExplanation = null, //Only available for online
            affixes = null //Only available for online
        )
    }
}

