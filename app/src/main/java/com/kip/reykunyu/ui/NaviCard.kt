@file:OptIn(ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun NaviCard(navi: Navi, naviClick: (String) -> Unit) {
    val expandable = !navi.seeAlso.isNullOrEmpty() || !navi.pronunciation.isNullOrEmpty() ||
                !navi.status_note.isNullOrEmpty() || !navi.status.isNullOrEmpty() ||
                !navi.meaning_note.isNullOrEmpty() || !navi.etymology.isNullOrEmpty() ||
                !navi.image.isNullOrEmpty() || !navi.infixes.isNullOrEmpty() ||
                !navi.source.isNullOrEmpty()

    var expanded by remember { mutableStateOf(false)} //expanded

    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    ElevatedCard(
        onClick = {if(expandable) expanded = !expanded},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            //Navi + Info
            FlowRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 7.dp)
            ) {

                //Na'vi
                val textStyleNavi = Typography.titleLarge
                Text(
                    text = navi.word,
                    style = textStyleNavi,
                    /*modifier = Modifier
                        .widthIn(min = 0.dp, max = 220.dp)*/
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
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    ){
                        AnimatedContent(targetState = showTypeInfo) { show ->
                            if (show) {
                                Text(
                                    text = stringResource(id = navi.typeDetails()),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.displaySmall
                                        .copy(fontSize = 20.sp)
                                )
                            }else {
                                Text(
                                    text = navi.typeDisplay(),
                                    style = MaterialTheme.typography.displaySmall
                                        .copy(fontSize = 20.sp)
                                )
                            }
                        }
                    }
                }
            }

            //Pronunciation
            AnimatedVisibility(visible = expanded) {
                if (navi.pronunciation != null) {
                    for (pronunciation in navi.pronunciation) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
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
                                        onClick = {/* TODO */ },
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

            AnimatedVisibility(visible = expanded) {
                //meaning note
                if (navi.meaning_note != null) {
                    RichText(content = navi.meaning_note, naviClick = naviClick)
                }

                AutoSpacer(navi.translations, navi.meaning_note, 5.dp, divider = false)


                //etymology
                InfoModule(category = "ETYMOLOGY", content = navi.etymology, naviClick = naviClick)

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
                            NaviReferenceChip(
                                naviUnformatted = refNavi, onClick = naviClick,
                                paddingR = 10.dp
                            )
                        }
                    }
                }


                AutoSpacer(navi.etymology, navi.seeAlso, divider = false)


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

                AutoSpacer(navi.infixes, padding = 8.dp)

                //status
                InfoModule(
                    category = "STATUS", content = navi.status?.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    naviClick = naviClick
                )

                //statusNote
                InfoModule(
                    category = "NOTE", content = navi.status_note, padding = 3.dp,
                    naviClick = naviClick
                )

                AutoSpacer(navi.status, navi.status_note, padding = 3.dp, divider = false)

                //Sources
                SourcesCard(sources = navi.source, naviClick = naviClick)

            }
            Spacer(Modifier.padding(6.dp))


            //EXPAND AND FOLD BUTTON
            if(expandable){
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clickable {
                            expanded = !expanded
                            Log.i(
                                "REYKUNYUFLOOD",
                                navi.seeAlso.toString() + navi.pronunciation.toString() + navi.status_note.toString() +
                                        navi.status.toString() + navi.meaning_note.toString() + navi.etymology.toString() +
                                        navi.image.toString() + navi.infixes.toString() + navi.source.toString()
                            )
                        }
                ) {

                    AnimatedContent(targetState = expanded) { expandState ->
                        Text(
                            text = if(!expandState) "VIEW MORE" else "VIEW LESS",
                            style = labelLarge,
//                        color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    AnimatedContent(targetState = expanded) { expandState ->
                        Icon(
                            imageVector = if (!expandState)
                                Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
//                        tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }


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
    padding: Dp = 8.dp,
    naviClick: (String) -> Unit
) {
    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    if (content == null) {
        return
    }

    Spacer(Modifier.padding(padding))
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

@Composable
fun <T> AutoSpacer(
    vararg elements: T,
    padding: Dp = 5.dp,
    divider: Boolean = true
) {
    if (elements.any{x -> x != null}) {
        Spacer(Modifier.padding(padding * .7f))
        if (divider) {
            Divider(modifier = Modifier.padding(horizontal = 20.dp))
        }
        Spacer(Modifier.padding(padding * .3f))
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RichText(
    content: String,
    style: TextStyle = Typography.bodyLarge,
    richText: List<RichTextComponent>? = null,
    padding: Boolean = true,
    naviClick: (String) -> Unit
) {

    //URL
    val context = LocalContext.current

    FlowRow(
        modifier = if (padding) Modifier.padding(horizontal = 20.dp) else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Rich Text

        for (component in richText ?: createRichText(content)) {
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
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            val builder = CustomTabsIntent.Builder()
                            val customTabsIntent = builder.build()

                            //Contains https and http
                            val url = if (component.content.contains("http")) {
                                component.content
                            } else {
                                "https://" + component.content
                            }

                            // Launch in browser-in-app
                            customTabsIntent.launchUrl(context,
                                Uri.parse(url))
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

@OptIn(ExperimentalMaterial3Api::class)
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
        onClick = {
            Log.i("REYKUNYU", "Na'vi card clicked! $refNavi")
            onClick(refNavi)
                  },
        label = {
            Text(
                text = refNavi,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        modifier = Modifier
            .defaultMinSize(minHeight = 0.dp),
    )
    Spacer(modifier = Modifier
        .padding(horizontal = paddingR)
        .defaultMinSize(minHeight = 0.dp))
}

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

    @Suppress("RegExpRedundantEscape")
    val spaceRegex = """((?<=[ \n\(])|(?=[ \n\)]))"""
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesCard(
    sources: List<List<String>>?,
    style: TextStyle = Typography.bodyLarge,
    naviClick: (String) -> Unit
) {
    if (sources.isNullOrEmpty()) {
        return
    }

    Spacer(Modifier.padding(6.dp))

    val sourcesClean = mutableListOf<List<String>>()
    
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
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp)
        ) {
            //Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = "SOURCES",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
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

            AnimatedVisibility(visible = expanded) {
                Column (Modifier.animateContentSize()) {
                    for ((index, source) in sourcesClean.withIndex()) {
                        // "either a string describing the source,
                        // or an array containing a description and an URL." (navi-tsim)
                        Column(
                            Modifier.padding(end = 10.dp)
                        ) {
                            Row(
                                Modifier.padding(bottom = 2.dp)
                            ) {
                                Text(
                                    "${index + 1}.",
                                    style = style.copy(fontWeight = FontWeight.Bold)
                                )

                                // Check if URL with title or just URLs
                                if (source.size == 2 &&
                                    !Patterns.WEB_URL.matcher(source[0]).matches() &&
                                    Patterns.WEB_URL.matcher(source[1]).matches()
                                ) {
                                    // URL
                                    RichText(
                                        content = "",
                                        naviClick = { /* UNUSED */ },
                                        richText = listOf(
                                            RichTextComponent(
                                                RichTextComponent.Type.Url,
                                                source[1],
                                                AnnotatedString(text = source[0])
                                            )
                                        ),
                                        padding = false
                                    )
                                } else {
                                    // Rich Text
                                    Column {
                                        for (entry in source) {
                                            RichText(
                                                content = entry,
                                                naviClick = naviClick,
                                                padding = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.padding(6.dp))
                    }
                    Spacer(Modifier.padding(3.dp))
                }
            }
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
    val naviList = listOf<Navi>(
        Navi(
            word = "skxawng skxa wng skxa wng. skxaw ng ì ì",
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
                listOf("here's some text. (https://en.wiktionary.org/wiki/Appendix:Na'vi)",
                    "also, here's reykunyu.lu",
                    "Frommer"),
            ),
            etymology = "Shortened form of ['eveng:n]. https://www.wikipedia.com",
            infixes = "h.angh.am",
            meaning_note = null/*"Used together with [zun:conj]. Check out reykunyu.lu and [skxawng:n]!"*/,
            seeAlso = listOf("oeng:pn", "oe:pn"),
            status = "unconfirmed",
            status_note = "Not yet officially confirmed by Pawl.",
            image = "toruk.png"
        )
    )
    LazyColumn {
        items(items = naviList) { item ->
            NaviCard(item, {})
        }
    }
}