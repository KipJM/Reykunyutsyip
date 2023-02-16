@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
                RichText(content = navi.meaning_note, naviClick = {/*TODO*/})
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
                        NaviReferenceChip(naviUnformatted = refNavi, onClick = {/*TODO*/},
                            paddingR = 10.dp)
                    }
                }
            }


            //status
            InfoModule(category = "STATUS", content = navi.status?.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))

            //statusNote
            InfoModule(category = "NOTE", content = navi.status_note)


            //Sources
            SourcesCard(sources = navi.source)

            Spacer(Modifier.padding(6.dp))
        }
    }
}


//region Rich Text

@Composable
fun InfoModule(
    category: String,
    content: String?,
    style: TextStyle = Typography.bodyLarge,
    naviClick: (String) -> Unit = {}
) {
    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    if (content == null) {
        return
    }

    Spacer(Modifier.padding(6.dp))
    Text(
        text = category.uppercase(),
        style = labelLarge,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    RichText(
        content = content,
        style = style,
        naviClick = naviClick
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RichText(
    content: String,
    style: TextStyle = Typography.bodyLarge,
    naviClick: (String) -> Unit
) {

    //URL
    val uriHandler = LocalUriHandler.current

    FlowRow(
        Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Rich Text
        for (component in createRichText(content)) {
            when (component.type) {
                RichTextComponent.Type.Text -> {
                    Text(
                        text = component.content,
                        style = style
                    )
                }
                RichTextComponent.Type.Url -> {
                    ClickableText(
                        text = component.url!!,
                        style = style.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        onClick = {
                            if (!component.url.startsWith("http://") &&
                                !component.url.startsWith("https://")
                            ) {
                                uriHandler.openUri("https://" + component.content)
                            }
                            else {
                                uriHandler.openUri(component.content)
                            }
                        }
                    )
                }
                RichTextComponent.Type.NaviRef -> {
                    NaviReferenceChip(
                        naviUnformatted = component.content,
                        paddingL = 1.dp,
                        paddingR = 1.dp,
                        onClick = naviClick
                    )
                }
                RichTextComponent.Type.Space -> {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}

@Composable
fun NaviReferenceChip(
    naviUnformatted: String,
    paddingL: Dp = 0.dp, paddingR: Dp = 0.dp,
    onClick: (String) -> Unit
) {
    // Remove the starting and ending brackets [ ]
    val refNavi = naviUnformatted.removePrefix("[").removeSuffix("]")
        .split(':', ignoreCase = true)[0] // Removes the type at the end
    // Example: [skxawng:n] -> skxawng

    Spacer(modifier = Modifier
        .padding(horizontal = paddingL)
        .defaultMinSize(minHeight = 0.dp))
    AssistChip(
        onClick = { onClick(refNavi) },
        label = {
            Text(
                text = refNavi,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        modifier = Modifier
            .defaultMinSize(minHeight = 0.dp)
        ,
    )
    Spacer(modifier = Modifier
        .padding(horizontal = paddingR)
        .defaultMinSize(minHeight = 0.dp))
}

@OptIn(ExperimentalTextApi::class)
fun createRichText(text: String): List<RichTextComponent> {
    val richText = mutableListOf<RichTextComponent>()

    //URL


    //Na'vi
    @Suppress("RegExpRedundantEscape")
    val naviSplitRegex = """((?<=\[.[^\[\]]{1,9999}\])|(?=\[.[^\[\]]*\]))"""
        .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    @Suppress("RegExpRedundantEscape")
    val naviRegex = """\[.[^\[\]]*\]"""
        .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    val spaceRegex = """((?<=[ \n])|(?=[ \n]))"""
        .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    // We first split the string based on URL, then based on Na'vi ref.
    // Then we just check whether it's a URL/Na'vi block, and assign the types accordingly
    val textPartitions = naviSplitRegex.split(text).toList()

    for (partition in textPartitions) {
//        if (urlRegex.matches(partition)) {
//            //URL
//            richText.add(RichTextComponent(
//                RichTextComponent.Type.Url,
//                partition,
//                buildAnnotatedString {
//                    this.append(partition)
//                    this.addUrlAnnotation(UrlAnnotation(partition),
//                        0, partition.length - 1)
//                }
//            ))
//        }
        if (naviRegex.matches(partition)) {
            //Na'vi
            richText.add(RichTextComponent(RichTextComponent.Type.NaviRef, partition))
        }
        else {
            //Text
            /*
            * TODO: For some reason text won't wrap with the Na'vi chips. For now I'm going to
            *  force the text to wrap by splitting every word :/
            */

            for (words in spaceRegex.split(partition)) {
                if (Patterns.WEB_URL.matcher(words).matches()) {
                    richText.add(RichTextComponent(
                        RichTextComponent.Type.Url,
                        words,
                        AnnotatedString(text=words)
                    ))
                }else {
                    if (words == " ") {
                        richText.add(RichTextComponent(RichTextComponent.Type.Space, words))
                    }else {
                        richText.add(RichTextComponent(RichTextComponent.Type.Text, words))
                    }
                }
            }
        }
    }
    
    return richText.toList()
}

data class RichTextComponent(
    val type: Type,
    val content: String,
    val url: AnnotatedString? = null
) {
    enum class Type {
        Text,
        Url,
        NaviRef,
        Space
    }
}

//endregion

@Composable
fun SourcesCard(sources: List<List<String>>?) {
    if (sources.isNullOrEmpty()) {
        return
    }

    Spacer(Modifier.padding(6.dp))

    val sourcesClean = mutableListOf<String>()
    
    //Remove random empty elements (List cleanup)
    for (source in sources) {
        val sourceClean = source.filter { it.isNotBlank() }
        
        if (sourceClean.isNotEmpty()) {
            sourcesClean += sourceClean
        }
    }

    //Collapsable card
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = {expanded = !expanded},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Column() {
            //Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 20.dp, end = 10.dp)
            ) {
                Text(
                    text = "SOURCES",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    if (expanded) {
                       Icon(Icons.Filled.KeyboardArrowDown, "fold sources")
                    } else {
                        Icon(Icons.Filled.KeyboardArrowUp, "expand sources")
                    }
                }
            }
        }
    }
    
    // either a string describing the source, or an array containing a description and an URL.
    for (source in sourcesClean) {
        // TODO
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
        etymology = "Shortened form of ['eveng:n]. https://www.wikipedia.com",
        infixes = "h.angh.am",
        meaning_note = "Used together with [zun:conj]. Check out reykunyu.lu and [skxawng:n]!",
        seeAlso = listOf("oeng:pn", "oe:pn"),
        status = "unconfirmed",
        status_note = "Not yet officially confirmed by Pawl.",
        image = "toruk.png"
    ))
}