@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.*
import com.kip.reykunyu.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NaviCard(navi: Navi) {
    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            //Navi + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 7.dp)
            ) {
                //Navi
                Text(
                    text = navi.word,
                    style = Typography.titleLarge
                )

                // WordType
                var showTypeInfo by remember { mutableStateOf(false) }
                Spacer(modifier = Modifier.padding(5.dp))
                Card(
                    onClick = {
                        showTypeInfo = !showTypeInfo
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ){
                        if (showTypeInfo) {
                            Text(
                                text = stringResource(id = navi.typeDetails()),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }else {
                            Text(
                                text = navi.typeDisplay(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            //Pronunciation
            if (navi.pronunciation != null) {
                for (pronunciation in navi.pronunciation) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //syllables
                        Text(
                            text = "(",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stylePronunciationText(
                                pronunciation.syllables, pronunciation.stressed
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        //Audio players
                        if ((pronunciation.audio?.size ?: 0) > 0) {
                            Spacer(modifier = Modifier.weight(1f))
                            for (audio in pronunciation.audio!!) {
                                AssistChip(
                                    onClick = {/* TODO */},
                                    label = {
                                        Text(
                                            text = audio.speaker,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(id = R.drawable.baseline_speaker_24),
                                            contentDescription = "Play audio by ${audio.speaker}",
                                            Modifier.size(AssistChipDefaults.IconSize)
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.weight(.1f))
                            }
                        }
                    }
                }
            }

            //Translations
            Spacer(Modifier.padding(3.dp))
            Divider()
            Spacer(Modifier.padding(6.dp))

            Text(
                text = "TRANSLATIONS",
                style = labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            var i = 1
            for (translation in navi.translations) {
                //TODO: Settings provider
                if (translation[UniversalSearchRepository.language] != null) {
                    if (i != 1)
                        Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        text = translation[UniversalSearchRepository.language]!!,
                        style = Typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                    )
                    i++
                }
            }

            //meaning note
            if (navi.meaning_note != null) {
                Text(
                    text = annotateRichText(navi.meaning_note),
                    style = Typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                )
                Spacer(Modifier.padding(2.dp))
            }

            //etymology
            InfoModule(category = "ETYMOLOGY", content = navi.etymology)

            //Infixes
            if (navi.infixes != null) {
                Spacer(Modifier.padding(6.dp))
                Text(
                    text = "INFIXES",
                    style = labelLarge,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Text(
                    text = navi.infixes.replace('.', '·'),
                    style = Typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                )

            }

            //See also
            if (navi.seeAlso != null) {
                Spacer(Modifier.padding(6.dp))
                Text(
                    text = "SEE ALSO",
                    style = labelLarge,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                FlowRow(
                    Modifier.padding(horizontal = 20.dp)
                ) {
                    for (refNavi in navi.seeAlso) {
                        AssistChip(
                            onClick = { /*TODO*/ },
                            label = {
                                Text(
                                text = refNavi,
                                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                )
                            },
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                    }
                }
            }


            //status
            InfoModule(category = "STATUS", content = navi.status?.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))

            //statusNote
            InfoModule(category = "NOTE", content = navi.status_note)




            Spacer(Modifier.padding(6.dp))
        }
    }
}


@Composable
fun InfoModule(category: String, content: String?, style: TextStyle = Typography.bodyLarge) {
    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    if (content != null) {
        Spacer(Modifier.padding(6.dp))
        Text(
            text = category.uppercase(),
            style = labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Text(
            text = annotateRichText(content),
            style = style,
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )

    }
}


fun stylePronunciationText(text: String, stressed: Int?): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(text)

    if (stressed == null)
        return builder.toAnnotatedString() //Some words don't have stress information. idk

    //"Pronunciations of the word, in which syllable breaks are indicated as dashes." - navi-tsim
    //index starts at 1.
    val stressedIndex = stressed - 1
    val syllables = text.split('-')

    if (syllables.size == 1)
        return builder.toAnnotatedString() //no underline if only one syllable

    var startIndex = 0
    for (i in 0 until stressedIndex ) {
        // index + wordCount
        // + 1(add dash)
        startIndex += syllables[i].length + 1
    }

    //End of the syllable index
    val endIndex: Int = startIndex + syllables[stressedIndex].length


    val stressedStyle = SpanStyle(
        textDecoration = TextDecoration.Underline
    )
    builder.addStyle(stressedStyle, startIndex, endIndex)

    return builder.toAnnotatedString()
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun annotateRichText(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(text)

    //URL
    val urlStyle = SpanStyle(
        textDecoration = TextDecoration.Underline
    )

    //Find URL links
    val urlRegex = """(http(s)?://.)?(www\.)?[-a-zA-Z\d@:%._+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z\d'@:%_+.~#?&/=]*)[^.]""".
    toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE, RegexOption.IGNORE_CASE))

    for (url in urlRegex.findAll(text)) {
        builder.addUrlAnnotation(UrlAnnotation(url.value), url.range.first, url.range.last)
        builder.addStyle(urlStyle, url.range.first, url.range.last)
    }


    //Na'vi
    val naviRegex = """\[\S+]""".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val naviTag = "NAVI"

    val naviStyle = SpanStyle(
        textDecoration = TextDecoration.Underline,
        background = MaterialTheme.colorScheme.secondaryContainer,
        fontWeight = FontWeight.Bold
    )

    //TODO: Convert Na'vi ref to buttons.
    for (navi in naviRegex.findAll(text)) {
        builder.addStringAnnotation(naviTag, navi.value.substring(1, navi.value.length - 2),
            navi.range.first, navi.range.last + 1)
        builder.addStyle(naviStyle, navi.range.first, navi.range.last + 1)
    }

    return builder.toAnnotatedString()
}


@Preview
@Composable
fun NaviCardPreview() {
    NaviCard(
        Navi(
        word = "skxawng",
        type = "ctr",
        pronunciation = listOf(
            Pronunciation(
                "s-xa-wng-s", stressed = 3,
                audio = listOf(
                    Audio("Plumps", "plumps/skxawng:n.mp3"),
                    Audio("tsyili", "tsyili/skxawng:n.mp3")
                )
            )
        ),
        translations = listOf(
            mapOf(Language.English to "Idiot, moron. a.k.a. Jake Sully")
        ),
        source = listOf(
            listOf("https://en.wiktionary.org/wiki/Appendix:Na'vi", "", ""),
            listOf("Taronyu's Dictionary 9.661 < Frommer",
                "https://en.wiktionary.org/wiki/Appendix:Na'vi"),
        ),
        etymology = "Shortened form of ['eveng:n].",
        infixes = "h.angh.am",
        meaning_note = "Used together with [zun:conj]",
        seeAlso = listOf("oeng:pn", "oe:pn"),
        status = "unconfirmed",
        status_note = "Not yet officially confirmed by Pawl.",
        image = "toruk.png"
    ))
}