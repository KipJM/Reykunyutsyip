package com.kip.reykunyu.data.dict

import com.kip.reykunyu.data.dict.*
import kotlinx.serialization.*


@Serializable
data class OnlineNaviRaw(
    @SerialName("na'vi")
    val navi: String,
    @SerialName("type")
    val wordType: String,

    val translations: List<Map<String, String>>,
    val shortTranslation: String? = null,

    val conjugated: List<ConjugatedElementRaw>? = null,
    val affixes: List<AffixRaw>? = null,

    val pronunciation: List<Pronunciation>? = null,

    val infixes: String? = null,

    val meaningNote: List<RichTextPartitionRaw>? = null,
    val etymology: List<RichTextPartitionRaw>? = null,

    val seeAlso: List<OnlineNaviRaw>? = null,
    val derived: List<OnlineNaviRaw>? = null,

    val image: String? = null,

    val status: String? = null,
    val statusNote: String? = null,

    val source: List<List<String>>? = null,
    //TODO: affixes, conjugated, conjugations, sentences

) {
    fun toNavi(): Navi {
        //modification is now done universely at UI layer
        val word = navi

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


        val convertedMeaningNote = RichText.create(meaningNote)
        val meaningNoteList =
            if(convertedMeaningNote != null)
            { listOf(convertedMeaningNote)
            } else { null }


        return Navi(
            word = word,
            type = wordType,
            translations = outputTranslation.toList(),
            pronunciation = pronunciation,
            infixes = infixes,
            meaningNote = meaningNoteList,
            etymology = RichText.create(etymology),
            seeAlso = seeAlso?.map { o -> o.navi },
            derived = derived?.map { o -> o.navi },
            image = image,
            status = status,
            statusNote = RichText.create(statusNote),
            source = Source.createList(source),
            conjugatedExplanation = if(conjugated != null) ConjugatedElementRaw.createExplanations(conjugated) else null,
            affixes = if(affixes != null) AffixRaw.createTable(affixes) else null
        )
    }
}

