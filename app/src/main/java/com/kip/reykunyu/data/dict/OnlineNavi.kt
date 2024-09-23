package com.kip.reykunyu.data.dict

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder


@Serializable(with = AdaptiveRichTextRawSerializer::class)
data class AdaptiveRichTextRaw(
    val text: String? = null,

    val richText: List<RichTextPartitionRaw>? = null
) {
    fun convertToRichText(): RichText? {
        if (text != null) {
            return RichText.create(text)
        }
        if (richText != null) {
            return RichText.create(richText)
        }
        //else
        return null
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = AdaptiveRichTextRaw::class)
object AdaptiveRichTextRawSerializer : KSerializer<AdaptiveRichTextRaw> {
    override fun deserialize(decoder: Decoder): AdaptiveRichTextRaw {
        val json = (decoder as JsonDecoder).decodeJsonElement()

        //Either it's Rich text, or plain text
        return try {
            AdaptiveRichTextRaw(text = Json.decodeFromString<String>(json.toString()))
        } catch (e: Exception) {
            AdaptiveRichTextRaw(richText = Json.decodeFromString<List<RichTextPartitionRaw>>(json.toString()))
        }

    }
}

@Serializable(with = AdaptiveNaviRefSerializer::class)
data class AdaptiveNaviRef(
    val text: String? = null,

    val navi: OnlineNaviRaw? = null
) {
    fun getNaviWord(): String? {
        if (text != null) {
            return text
        }
        if (navi != null) {
            return navi.navi
        }
        //else
        return null
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = AdaptiveNaviRef::class)
object AdaptiveNaviRefSerializer : KSerializer<AdaptiveNaviRef> {
    override fun deserialize(decoder: Decoder): AdaptiveNaviRef {
        val json = (decoder as JsonDecoder).decodeJsonElement()

        //Either it's Rich text, or plain text
        return try {
            AdaptiveNaviRef(text = Json.decodeFromString<String>(json.toString()))
        } catch (e: Exception) {
            AdaptiveNaviRef(navi = Json.decodeFromString<OnlineNaviRaw>(json.toString()))
        }

    }
}



@Serializable
data class OnlineNaviRaw(
    @SerialName("na'vi")
    val navi: String,
    @SerialName("type")
    val wordType: String,

    val word: Map<String, String>?,
    val word_raw: Map<String, String>?,

    val translations: List<Map<String, String>>,
    @SerialName("short_translation")
    val shortTranslation: String? = null,

    val conjugated: List<ConjugatedElementRaw>? = null,
    val affixes: List<AffixRaw>? = null,

    val pronunciation: List<Pronunciation>? = null,

    val infixes: String? = null,

    val meaningNote: AdaptiveRichTextRaw? = null,
    val etymology: AdaptiveRichTextRaw? = null,

    val seeAlso: List<AdaptiveNaviRef>? = null,
    val derived: List<AdaptiveNaviRef>? = null,

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


        val convertedMeaningNote = meaningNote?.convertToRichText()
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
            etymology = etymology?.convertToRichText(),
            seeAlso = seeAlso?.map { o -> o.getNaviWord() ?: ""},
            derived = derived?.map { o -> o.getNaviWord() ?: ""},
            image = image,
            status = status,
            statusNote = RichText.create(statusNote),
            source = Source.createList(source),
            conjugatedExplanation = if(conjugated != null) ConjugatedElementRaw.createExplanations(conjugated) else null,
            affixes = if(affixes != null) AffixRaw.createTable(affixes) else null
        )
    }
}

