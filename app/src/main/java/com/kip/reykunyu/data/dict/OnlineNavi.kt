package com.kip.reykunyu.data.online

import com.kip.reykunyu.data.dict.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
//Rich text component: Either plain text, or a Na'vi ref chip
data class RichTextComponentRaw(
    val text: String? = null,

    // A special Na'vi structure that only has basic ref info,
    // Thankfully no recursion loop will happen. This should be enforced on Reykunyu's side.
    // If not, bad things WILL happen.
    val naviRef: OnlineNaviRaw? = null
)

@Serializable
data class OnlineNaviRaw(
    @SerialName("na'vi")
    val navi: String,
    @SerialName("type")
    val wordType: String,

    val translations: List<Map<String, String>>,
    val pronunciation: List<Pronunciation>? = null,

    val infixes: String? = null,

    val meaning_note: List<String>? = null,
    val etymology: List<RichTextComponentRaw>? = null,

    val seeAlso: List<String>? = null,
    val derived: List<OnlineNaviRaw>? = null,

    val image: String? = null,

    val status: String? = null,
    val status_note: String? = null,

    val source: List<List<String>>? = null,
    //TODO: affixes, conjugated, conjugations, sentences

) {
    fun toNavi(): Navi {
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


        return Navi(
            word = word,
            type = wordType,
            translations = outputTranslation.toList(),
            pronunciation = pronunciation,
            infixes = infixes,
            meaning_note = meaning_note?.mapNotNull { o -> RichText.create(o) },
            etymology = RichText.create(etymology),
            seeAlso = null,
            derived = derived?.map { o -> o.navi },
            image = image,
            status = status,
            status_note = RichText.create(status_note),
            source = Source.createList(source),
        )
    }
}