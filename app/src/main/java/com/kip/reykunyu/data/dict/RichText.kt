package com.kip.reykunyu.data.dict

import android.util.Patterns
import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder


@Serializable(with = RichTextComponentRawSerializer::class)
//Rich text component: Either plain text, or a Na'vi ref chip
data class RichTextPartitionRaw(
    val text: String? = null,

    // A special Na'vi structure that only has basic ref info,
    val naviRef: OnlineNaviRaw? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = RichTextPartitionRaw::class)
object RichTextComponentRawSerializer : KSerializer<RichTextPartitionRaw> {
    override fun deserialize(decoder: Decoder): RichTextPartitionRaw {
        val json = (decoder as JsonDecoder).decodeJsonElement()

        //Either it's a text(URL) element, or a Na'vi ref element
        return try {
            RichTextPartitionRaw(text = Json.decodeFromString<String>(json.toString()))
        } catch (e: Exception) {
            RichTextPartitionRaw(naviRef = Json.decodeFromString<OnlineNaviRaw>(json.toString()))
        }

    }
}

data class RichText(
    val sequence: List<Partition>
) {
    data class Partition(
        val type: Type,
        val text: String? = null,
        val navi: String? = null,
        val urlDisplay: AnnotatedString? = null,
        val url: String? = null
    ) {
        enum class Type {
            Text,
            Url,
            Navi,
            Space
        }
    }

    companion object{

        @Suppress("RegExpRedundantEscape")
        val spaceRegex = """((?<=[ \n\(])|(?=[ \n\)]))"""
            .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))


        fun create(raw: List<RichTextPartitionRaw>?): RichText? {
            if(raw == null) {
                return null
            }

            val sequence = mutableListOf<Partition>()

            for (partition in raw) {
                //Na'vi
                if (partition.naviRef != null) {
                    sequence.add(Partition(Partition.Type.Navi,
                        navi = partition.naviRef.navi))
                    continue
                }

                /*
                * TODO: For some reason text won't wrap with the Na'vi chips. For now
                *  I'm going to force the text to wrap by splitting every word :/
                */
                // TEXT or URL
                if (partition.text != null) {
                    for (words in spaceRegex.split(partition.text)) {
                        when {
                            //URL
                            Patterns.WEB_URL.matcher(words).matches() -> {
                                sequence.add(
                                    Partition(
                                        Partition.Type.Url,
                                        urlDisplay = AnnotatedString(text = words),
                                        url = words
                                    )
                                )
                            }

                            //Space
                            words.isBlank() ->
                                sequence.add(Partition(Partition.Type.Space, words))

                            //Text
                            else ->
                                sequence.add(Partition(Partition.Type.Text, words))
                        }
                    }
                }
            }
            return RichText(sequence)
        }


        /**
         * Converts plain string from Na'vi v1 into Rich Text
         */
        fun create(text: String?): RichText? {
            if (text.isNullOrEmpty()) {
                return null
            }

            val sequence = mutableListOf<Partition>()

            //Na'vi
            @Suppress("RegExpRedundantEscape")
            val naviSplitRegex = """((?<=\[.[^\[\]]{1,9999}\])|(?=\[.[^\[\]]*\]))"""
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

            @Suppress("RegExpRedundantEscape")
            val naviRegex = """\[.[^\[\]]*\]"""
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))


            // We first split the string based on URL, then based on Na'vi ref.
            // Then we just check whether it's a URL/Na'vi block, and assign the types accordingly
            val textPartitions = naviSplitRegex.split(text).toList()

            for (partition in textPartitions)
            {
                //Na'vi
                if (naviRegex.matches(partition)) {
                    // Remove the starting and ending brackets [ ]
                    val refNavi = partition.removePrefix("[").removeSuffix("]")
                        .split(':', ignoreCase = true)[0] // Removes the type at the end
                    // Example: [skxawng:n] -> skxawng

                    sequence.add(Partition(Partition.Type.Navi, navi = refNavi))
                    continue
                }


                /*
                * TODO: For some reason text won't wrap with the Na'vi chips. For now
                *  I'm going to force the text to wrap by splitting every word :/
                */
                // TEXT or URL
                for (words in spaceRegex.split(partition)) {

                    when {

                        //URL
                        Patterns.WEB_URL.matcher(words).matches() -> {
                            sequence.add(
                                Partition(
                                    Partition.Type.Url,
                                    urlDisplay = AnnotatedString(text = words),
                                    url = words
                                )
                            )
                        }

                        words == "\n" ->
                            sequence.add(Partition(Partition.Type.Text, words))

                        //Space
                        words.isBlank() ->
                            sequence.add(Partition(Partition.Type.Space, words))

                        //Text
                        else ->
                            sequence.add(Partition(Partition.Type.Text, words))
                    }

                }

            }

            return RichText(sequence)
        }
    }
}