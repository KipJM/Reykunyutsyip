package com.kip.reykunyu.data.dict

import android.util.Log
import kotlinx.serialization.Serializable
import java.util.zip.DataFormatException

///Conjugated explanation and affixes list

@Serializable
data class ConjugatedElementRaw(
    val type: String,
    val conjugation: Conjugation
) {
    @Serializable
    data class Conjugation(
        val result: List<String>,
        val root: String,
        val affixes: List<String>? = null,
        val infixes: List<String>? = null,
        val correction: String? = null,
        val form: String? = null
    )

    // https://github.com/Willem3141/navi-reykunyu/blob/master/fraporu/ayvefya/reykunyu.js#L146
    companion object {
        fun createExplanations(raw: List<ConjugatedElementRaw>): List<ConjugatedExplanation> {

            val explanations = mutableListOf<ConjugatedExplanation>()

            for (element in raw) {

                val conjugation = element.conjugation

                if(conjugation.result.count() == 1
                    && conjugation.result[0].lowercase() == conjugation.root.lowercase()
                    && conjugation.correction == null
                ) {
                    continue
                }

                val explanation: ConjugatedExplanation = when (element.type)
                {
                    "n" -> {
                        explainNoun(conjugation)
                    }

                    "v" -> {
                        explainVerb(conjugation)
                    }

                    "adj" -> {
                        explainAdjective(conjugation)
                    }

                    "v_to_n" -> {
                        explainVerbToNoun(conjugation)
                    }

                    "v_to_adj" -> {
                        explainVerbToAdjective(conjugation)
                    }

                    "v_to_part" -> {
                        explainVerbToParticiple(conjugation)
                    }

                    "adj_to_adv" -> {
                        explainAdjectiveToVerb(conjugation)
                    }

                    else -> {
                        Log.wtf(
                            "REYKUNYU", "Parsing Na'vi broke. ${element.type} " +
                                    "not expected in the possible conjugation types"
                        )
                        throw DataFormatException()
                    }
                }

                explanations.add(explanation)

            }
            return explanations
        }


        //region Conversion parsing

        private fun explainNoun(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()
            val affixes = conjugation.affixes!!


            for (i in 0..2) {

                if (affixes[i].isNotBlank()) {
                    partitions.add(
                        ConjugatedExplanation.Partition
                        (
                            ConjugatedExplanation.Partition.Type.Prefix, affixes[i])
                    )
                }
            }

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            for (i in 3..6 ) {
                if (affixes[i].isNotBlank()) {
                    partitions.add(ConjugatedExplanation.Partition
                        (
                        ConjugatedExplanation.Partition.Type.Suffix, affixes[i])
                    )
                }
            }


            // Correction & Result
            if (!conjugation.correction.isNullOrBlank()) {
                partitions.add(ConjugatedExplanation.Partition
                    (
                    ConjugatedExplanation.Partition.Type.Correction, conjugation.correction)
                )
            }

            return ConjugatedExplanation(partitions, conjugation.result, null)
        }

        private fun explainVerb(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()
            val infixes = conjugation.infixes!!


            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            for (i in 0..2) {
                if (infixes[i].isNotBlank()) {
                    partitions.add(ConjugatedExplanation.Partition
                        (
                        ConjugatedExplanation.Partition.Type.Infix, infixes[i])
                    )
                }
            }


            // Correction & Result
            if (!conjugation.correction.isNullOrBlank()) {
                partitions.add(ConjugatedExplanation.Partition
                    (
                    ConjugatedExplanation.Partition.Type.Correction, conjugation.correction)
                )
            }

            return ConjugatedExplanation(partitions, conjugation.result, null)
        }

        private fun explainAdjective(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()

            if (conjugation.form == "postnoun") {
                partitions.add(ConjugatedExplanation.Partition
                    (
                    ConjugatedExplanation.Partition.Type.Prefix, "a")
                )
            }

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            if (conjugation.form == "prenoun") {
                partitions.add(ConjugatedExplanation.Partition
                    (
                    ConjugatedExplanation.Partition.Type.Suffix, "a")
                )
            }


            // Correction & Result
            if (!conjugation.correction.isNullOrBlank()) {
                partitions.add(ConjugatedExplanation.Partition
                    (
                    ConjugatedExplanation.Partition.Type.Correction, conjugation.correction)
                )
            }

            return ConjugatedExplanation(partitions, conjugation.result, null)
        }

        private fun explainVerbToNoun(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            partitions.add(ConjugatedExplanation.Partition
                (
                ConjugatedExplanation.Partition.Type.Suffix, conjugation.affixes!![0])
            )

            return ConjugatedExplanation(partitions, conjugation.result, "n")
        }

        private fun explainVerbToAdjective(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()

            partitions.add(ConjugatedExplanation.Partition
                (
                ConjugatedExplanation.Partition.Type.Prefix, conjugation.affixes!![0])
            )

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )


            return ConjugatedExplanation(partitions, conjugation.result, "adj")
        }

        private fun explainVerbToParticiple(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Infix, conjugation.affixes!![0])
            )

            return ConjugatedExplanation(partitions, conjugation.result, "adj")
        }

        private fun explainAdjectiveToVerb(conjugation: Conjugation): ConjugatedExplanation {
            val partitions = mutableListOf<ConjugatedExplanation.Partition>()

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Prefix, conjugation.affixes!![0])
            )

            partitions.add(ConjugatedExplanation.Partition
                (ConjugatedExplanation.Partition.Type.Root, conjugation.root)
            )

            return ConjugatedExplanation(partitions, conjugation.result, "adv")
        }
        //endregion

    }
}


data class ConjugatedExplanation(
    val formula: List<Partition>,
    val words: List<String>,
    val type: String?
) {
    data class Partition(
        val type: Type,
        val content: String,
    ) {
        enum class Type {
            Prefix,
            Root,
            Suffix,
            Infix,
            Correction
        }
    }

    companion object {
        fun getAffixType(type: String): Partition.Type {
            when (type) {
                "aff:pre" -> {
                    return Partition.Type.Prefix
                }

                "aff:in" -> {
                    return Partition.Type.Infix
                }

                "aff:suf" -> {
                    return Partition.Type.Suffix
                }

                else -> {
                    return Partition.Type.Root //generic
                }
            }
        }
    }
}


@Serializable
data class AffixRaw(
    val type: String,
    val combinedFrom: List<AffixRaw>? = null,
    val affix: RichTextPartitionRaw // Can either be plain text(no ref) or Na'vi ref
){
    companion object {
        fun createTable(affixes: List<AffixRaw>): List<AffixListElement> {
            val list = mutableListOf<AffixListElement>()
            for (affix in affixes) {
                if (!affix.combinedFrom.isNullOrEmpty()) {
                    //Since it's combined, there won't be a ref
                    val display = affix.affix.text!!
                    val type = "aff:in"

                    val components = mutableListOf<RichText.Partition>()
                    val meaning = mutableListOf<RichText.Partition>()


                    var first = true
                    for (part in affix.combinedFrom) {

                        if (first) {

                            components += RichText.Partition(
                                type = RichText.Partition.Type.Text,
                                text = "= "
                            )
                            first = false

                        } else {

                            components += RichText.Partition(
                                type = RichText.Partition.Type.Text,
                                text = " + "
                            )

                            meaning += RichText.Partition(
                                type = RichText.Partition.Type.Text,
                                text = " + "
                            )
                        }


                        val compPart = RichText.Partition.create(part.affix)
                        if (compPart != null) {
                            components.add(compPart)
                        }

                        meaning += RichText.Partition(
                            type = RichText.Partition.Type.LocalizedText,
                            localizedText = part.affix.naviRef!!.translations[0]
                        )
                    }


                    list += AffixListElement(
                        affix = display,
                        ref = false,
                        type = type,
                        components = RichText(components),
                        meaning = RichText(meaning)
                    )

                } else {

                    val display = affix.affix.naviRef!!.navi
                    val type = affix.affix.naviRef.wordType

                    list += AffixListElement(
                        affix = display,
                        ref = true,
                        type = type,
                        meaning = RichText(listOf(RichText.Partition(
                            type = RichText.Partition.Type.LocalizedText,
                            localizedText = affix.affix.naviRef.translations[0]
                        )))
                    )
                }
            }

            return list
        }
    }
}



data class AffixListElement(
    val affix: String,
    val ref: Boolean,
    val type: String,
    val components: RichText? = null,
    val meaning: RichText
)