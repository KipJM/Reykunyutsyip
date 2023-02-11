@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.Audio
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.Pronunciation
import com.kip.reykunyu.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaviCard(navi: Navi) {
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
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stylePronunciationText(
                                pronunciation.syllables, pronunciation.stressed
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
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

        }
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
        // index + wordCount - 1(convert to index)
        // + 1(add dash)
        startIndex += syllables[i].length
    }

    // Next character is the start of the stressed syllable, so add 1
    if(stressedIndex != 0)
        startIndex++
    //End of the syllable index
    val endIndex: Int = startIndex + syllables[stressedIndex].length - 1


    val stressedStyle = SpanStyle(
        textDecoration = TextDecoration.Underline
    )
    builder.addStyle(stressedStyle, startIndex, endIndex)

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
                "skxawng", stressed = 1,
                audio = listOf(
                    Audio("Plumps", "plumps/skxawng:n.mp3"),
                    Audio("tsyili", "tsyili/skxawng:n.mp3")
                )
            ),
            Pronunciation("ftxa-vang", stressed = 1,
                audio = listOf(
                    Audio("Plumps", "plumps/ftxavang:adj.mp3"),
                    Audio("tsyili", "tsyili/ftxavang:adj.mp3")
                )
            )
        ),
        translations = listOf(
            mapOf(Language.English to "moron, idiot"),
            mapOf(Language.English to "hrh, this is a test!"),
        ),
        source = listOf(
            listOf("http://en.wiktionary.org/wiki/Appendix:Na'vi", "", ""),
            listOf("Taronyu's Dictionary 9.661 < Frommer"),
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