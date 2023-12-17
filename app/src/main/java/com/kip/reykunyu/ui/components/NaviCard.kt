@file:OptIn(ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kip.reykunyu.R
import com.kip.reykunyu.data.api.AudioImageRepository
import com.kip.reykunyu.data.dict.AffixListElement
import com.kip.reykunyu.data.dict.Audio
import com.kip.reykunyu.data.dict.ConjugatedExplanation
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.Pronunciation
import com.kip.reykunyu.data.dict.RichText
import com.kip.reykunyu.data.dict.Source
import com.kip.reykunyu.data.dict.typeInfoMap
import com.kip.reykunyu.data.dict.typeMap
import com.kip.reykunyu.data.offline.DictNavi
import com.kip.reykunyu.data.online.OnlineTranslateSearch
import com.kip.reykunyu.ui.theme.Typography
import com.valentinilk.shimmer.shimmer


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun NaviCard(navi: Navi, language: Language, naviClick: (String) -> Unit, expanded: Boolean, toggleExpand: () -> Unit) {

    val expandable = !navi.seeAlso.isNullOrEmpty() || !navi.pronunciation.isNullOrEmpty() ||
                navi.statusNote != null || !navi.status.isNullOrEmpty() ||
                !navi.meaningNote.isNullOrEmpty() || navi.etymology != null ||
                !navi.image.isNullOrEmpty() || !navi.infixes.isNullOrEmpty() ||
                !navi.source.isNullOrEmpty()

    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)


    ElevatedCard(
        onClick = {if(expandable) toggleExpand()},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            //Navi + Info
            FlowRow(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 7.dp)
            ) {

                //Na'vi
                val textStyleNavi = Typography.titleLarge
                Text(
                    text = Navi.lemmaForm(navi.word, navi.type),
                    style = textStyleNavi,
                    /*modifier = Modifier
                        .widthIn(min = 0.dp, max = 220.dp)*/
                )

                // WordType
                Spacer(modifier = Modifier.padding(5.dp))
                WordTypeCard(navi.typeDetails(), navi.typeDisplay())
            }

            //Pronunciation
            AnimatedVisibility(expanded) {
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
                                    AudioChip(audio)
                                    Spacer(modifier = Modifier.weight(.1f))
                                }
                            }
                        }
                    }
                }
            }

            //Translations
            Spacer(Modifier.padding(3.dp))
            HorizontalDivider()
            Spacer(Modifier.padding(6.dp))

            Text(
                text = "TRANSLATIONS",
                style = labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            var i = 1
            for (translation in navi.translations) {
                //Displays the proper language
                if (i != 1)
                    Spacer(modifier = Modifier.padding(2.dp))

                Text(
                    text = translation[language] ?: translation[Language.English]!!,
                    style = Typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                )
                i++
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    //meaning note
                    if (navi.meaningNote != null) {

                        for (note in navi.meaningNote) {
                            RichTextComponent(
                                richText = note,
                                naviClick = naviClick,
                                language = language
                            )
                        }

                    }

                    //AutoSpacer(navi.translations, navi.meaning_note, 5.dp, divider = false)

                    // Conjugation explanation
                    if (navi.conjugatedExplanation != null) {

                        Spacer(Modifier.padding(8.dp))
                        Text(
                            text = "CONJUGATIONS",
                            style = labelLarge,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        val conjugationTextStyle = Typography.titleLarge.copy(fontSize = 24.sp)

                        navi.conjugatedExplanation.forEachIndexed { index, explanation ->
                            FlowRow(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                /*Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Filled.KeyboardArrowRight, null)
                                    Spacer(modifier = Modifier.weight(1f))
                                }*/

                                explanation.formula.forEachIndexed { index, partition ->


                                    val tooltip = when (partition.type) {
                                        ConjugatedExplanation.Partition.Type.Prefix -> "Prefix"
                                        ConjugatedExplanation.Partition.Type.Root -> ""
                                        ConjugatedExplanation.Partition.Type.Suffix -> "Suffix"
                                        ConjugatedExplanation.Partition.Type.Infix -> "Infix"
                                        ConjugatedExplanation.Partition.Type.Correction -> ""
                                    }

                                    if (partition.type == ConjugatedExplanation.Partition.Type.Root
                                        ||
                                        partition.type == ConjugatedExplanation.Partition.Type.Correction
                                    ) {
                                        Text(
                                            text = partition.content,
                                            style = conjugationTextStyle,
                                            color = getAffixColor(type = partition.type)
                                        )

                                    } else {
                                        TooltipBox(
                                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                            tooltip = { Text(tooltip) },
                                            state = rememberTooltipState()
                                        ) {
                                            Text(
                                                text = partition.content,
                                                style = conjugationTextStyle,
                                                color = getAffixColor(type = partition.type)
                                            )
                                        }
                                    }



                                    if (partition.type == ConjugatedExplanation.Partition.Type.Correction) {
                                        Text(
                                            text = " ",
                                            style = conjugationTextStyle
                                        )
                                    } else if (
                                        explanation.formula.count() == index + 2 &&
                                        explanation.formula.last().type == ConjugatedExplanation.Partition.Type.Correction
                                    ) {
                                        //The correction is on the other side of equal sign
                                        Text(
                                            text = " = ",
                                            style = conjugationTextStyle
                                        )
                                    } else if (index == explanation.formula.count() - 1) {
                                        //Last one
                                        Text(
                                            text = " = ",
                                            style = conjugationTextStyle
                                        )
                                    } else {
                                        Text(
                                            text = " + ",
                                            style = conjugationTextStyle
                                        )
                                    }
                                }

                                //Word display
                                explanation.words.forEachIndexed { index, word ->
                                    Text(
                                        text = word,
                                        style = conjugationTextStyle
                                    )
                                    if (index + 2 <= explanation.words.count()) {
                                        Text(
                                            text = " / ",
                                            style = conjugationTextStyle,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                //optional type display
                                if (explanation.type != null) {
                                    var showTypeInfo by remember { mutableStateOf(false) }
                                    Card(
                                        onClick = {
                                            showTypeInfo = !showTypeInfo
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            AnimatedContent(
                                                targetState = showTypeInfo,
                                                label = "type display"
                                            ) { show ->
                                                if (show) {
                                                    Text(
                                                        text = stringResource(
                                                            id = typeInfoMap[explanation.type]
                                                                ?: R.string.unknown
                                                        ),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.displaySmall
                                                            .copy(fontSize = 20.sp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = typeMap[explanation.type] ?: "?",
                                                        style = MaterialTheme.typography.displaySmall
                                                            .copy(fontSize = 20.sp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //Spacer
                            if (index < navi.conjugatedExplanation.count() - 1) {
                                Spacer(Modifier.padding(5.dp))
                            }
                        }
                    }

                    //Affixes
                    AffixTable(
                        affixes = navi.affixes,
                        show = expanded,
                        language = language,
                        naviClick = naviClick
                    )

                    //etymology
                    RichInfoModule(
                        category = "ETYMOLOGY",
                        richText = navi.etymology,
                        language = language,
                        naviClick = naviClick
                    )

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
                                    refNavi = refNavi, onClick = naviClick,
                                    paddingR = 10.dp,
                                    type = ""
                                )
                            }
                        }
                    }

                    //Derived
                    if (navi.derived != null) {
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = "DERIVED",
                            style = labelLarge,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        FlowRow(
                            Modifier.padding(horizontal = 20.dp)
                        ) {
                            for (refNavi in navi.derived) {
                                NaviReferenceChip(
                                    refNavi = refNavi, onClick = naviClick,
                                    paddingR = 10.dp,
                                    type = ""
                                )
                            }
                        }
                    }

                    AutoSpacer(navi.etymology, navi.seeAlso, navi.derived, divider = false)


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

                    AutoSpacer(navi.infixes, divider = false)

                    ImageModule(image = navi.image, naviWord = navi.word)

                    AutoSpacer(navi.infixes, navi.image, padding = 8.dp)

                    //status
                    TextModule(
                        category = "STATUS", content = navi.status?.uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black
                        ),
                        language = language
                    )

                    //statusNote
                    RichInfoModule(
                        category = "NOTE", richText = navi.statusNote, padding = 3.dp,
                        language = language,
                        naviClick = naviClick
                    )


                }
            }

            SourcesCard(sources = navi.source, show = expanded, naviClick = naviClick)


            Spacer(Modifier.padding(6.dp))


            //EXPAND AND FOLD BUTTON
            if (expandable) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .clip(shape = RoundedCornerShape(50))
                        .clickable {
                            toggleExpand()
                        }
                ) {

                    AnimatedContent(targetState = expanded, label = "EXPAND/HIDE") { expandState ->
                        Text(
                            text = if (!expandState) "VIEW MORE" else "VIEW LESS",
                            style = labelLarge,
//                        color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    AnimatedContent(targetState = expanded, label = "EXPAND/HIDE") { expandState ->
                        Icon(
                            imageVector = if (!expandState)
                                Icons.Filled.KeyboardArrowDown
                            else Icons.Filled.KeyboardArrowUp,
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

@Composable
private fun WordTypeCard(typeDetails: Int, typeDisplay: String) {
    var showTypeInfo by remember { mutableStateOf(false) }
    Card(
        onClick = {
            showTypeInfo = !showTypeInfo
        }
    ) {
        Row(
            modifier = Modifier
                .heightIn(max = 30.dp)
                .padding(horizontal = 8.dp)
                .fillMaxHeight()
                .offset(y = -7.dp) //TODO: hacky fix
        ) {
            AnimatedContent(targetState = showTypeInfo, label = "word type") { show ->
                if (show) {
                    Text(
                        text = stringResource(id = typeDetails),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall
                            .copy(fontSize = 20.sp),
                        modifier = Modifier
                    )
                } else {
                    Text(
                        text = typeDisplay,
                        style = MaterialTheme.typography.displaySmall
                            .copy(fontSize = 20.sp),
                        modifier = Modifier

                    )
                }
            }
        }
    }
}


//region modules

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AudioChip(audio: Audio) {
    val context = LocalContext.current

    var playing by remember {
        mutableStateOf(false)
    }
    var playRequested by remember {
        mutableStateOf(false)
    }


    InputChip(
        onClick = {
            playRequested = true
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                mediaPlayer.setDataSource(context,
                    AudioImageRepository.audioUrl.buildUpon().appendPath(audio.file).build()
                )

                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    Toast.makeText(context, "Failed to play audio!", Toast.LENGTH_SHORT).show()
                    Log.w("REYKUNYU", "ERROR while trying to play audio ${audio.file} " +
                            "($mp: $what, $extra)")

                    return@setOnErrorListener true
                }
                mediaPlayer.setOnCompletionListener { mp ->
                    playing = false
                    playRequested = false
                    mp.release()
                }
                mediaPlayer.setOnPreparedListener { mp ->
                    mp.start()
                    playing = true
                }

                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to play audio! ($e)", Toast.LENGTH_LONG).show()
                Log.w(
                    "REYKUNYU", "ERROR while trying to play audio ${audio.file} " +
                            "($e)"
                )
            }

        },
        label = {
            Text(
                text = audio.speaker,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                painterResource(id = R.drawable.baseline_speaker_24),
                contentDescription = "Play pronunciation from ${audio.speaker}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        selected = playing,
        modifier = if (playRequested && !playing) {
            Modifier.shimmer()
        } else {
            Modifier
        }
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
        }
        Spacer(Modifier.padding(padding * .3f))
    }
}


@Composable
fun RichInfoModule(
    category: String,
    richText: RichText?,
    style: TextStyle = Typography.bodyLarge,
    padding: Dp = 8.dp,
    language: Language,
    naviClick: (String) -> Unit
) {
    if (richText == null) {
        return
    }

    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    Spacer(Modifier.padding(padding))
    Text(
        text = category.uppercase(),
        style = labelLarge,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    RichTextComponent(
        richText = richText,
        style = style,
        naviClick = naviClick,
        language = language
    )
}

@Composable
fun TextModule(
    category: String,
    content: String?,
    style: TextStyle = Typography.bodyLarge,
    language: Language,
    padding: Dp = 8.dp,
) {
    if (content == null) {
        return
    }

    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    Spacer(Modifier.padding(padding))
    Text(
        text = category.uppercase(),
        style = labelLarge,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    RichTextComponent(
        richText = RichText(listOf(
            RichText.Partition(RichText.Partition.Type.Text, text = content)
        )),
        style = style,
        language = language,
        naviClick = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageModule(
    image: String?,
    naviWord: String,
    padding: Dp = 8.dp,
) {
    if (image == null) {
        return
    }

    val labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp)
    Spacer(Modifier.padding(padding))
    Text(
        text = "IMAGE",
        style = labelLarge,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = {
            Toast.makeText(context, R.string.image_copyright, Toast.LENGTH_SHORT).show()
        }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(AudioImageRepository.imageUrl.buildUpon().appendPath(image).build().toString())
                .crossfade(true)
                .build(),
            contentDescription = "A hand drawn illustration of $naviWord.",
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            placeholder = painterResource(id = R.drawable.baseline_downloading_24),
            modifier = Modifier.fillMaxSize()
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

//region Rich Text

@Composable
fun lintSpecialText(type: String? = ""): Color {
    if (type.isNullOrEmpty()) {
        return Color.Unspecified
    }

    return getAffixColor(type = ConjugatedExplanation.getAffixType(type))
}

@Composable
fun getAffixColor(type: ConjugatedExplanation.Partition.Type): Color {
    return when(type) {
        ConjugatedExplanation.Partition.Type.Prefix -> MaterialTheme.colorScheme.secondary
        ConjugatedExplanation.Partition.Type.Root -> Color.Unspecified
        ConjugatedExplanation.Partition.Type.Suffix -> MaterialTheme.colorScheme.primary
        ConjugatedExplanation.Partition.Type.Infix -> MaterialTheme.colorScheme.tertiary
        ConjugatedExplanation.Partition.Type.Correction -> MaterialTheme.colorScheme.error
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RichTextComponent(
    richText: RichText?,
    style: TextStyle = Typography.bodyLarge,
    padding: Boolean = true,
    language: Language,
    naviClick: (String) -> Unit
) {
    if(richText == null) {
        return
    }


    //URL
    val context = LocalContext.current

    FlowRow(
        modifier = if (padding) Modifier.padding(horizontal = 20.dp) else Modifier,
        verticalArrangement = Arrangement.Center
    ) {
        //Rich Text
        for (component in richText.sequence) {
            when (component.type) {

                RichText.Partition.Type.Text, RichText.Partition.Type.LocalizedText ->
                {
                    val text: String = if(component.type == RichText.Partition.Type.Text)
                        component.text!!
                    else
                        component.localizedText!![language.toString()]
                            ?: component.localizedText[Language.English.toString()]!!


                    //Hacky way of supporting line breaks
                    if (text == "\n"){
                        Spacer(Modifier.padding(horizontal = 100000.dp, vertical=2.dp))
                    }else {
                        Text(
                            text = text,
                            style = style,
                            color = lintSpecialText(component.naviType)
                        )
                    }
                }

                RichText.Partition.Type.Url ->
                {
                    ClickableText(
                        text = component.urlDisplay!!,
                        style = style.copy(
                            textDecoration = TextDecoration.Underline,
                            color = if (lintSpecialText(component.naviType) == Color.Unspecified) MaterialTheme.colorScheme.onPrimaryContainer else lintSpecialText(component.naviType)
                        ),
                        onClick = {
                            val builder = CustomTabsIntent.Builder()
                            val customTabsIntent = builder.build()

                            //Contains https and http
                            val url = if (component.url!!.contains("http")) {
                                component.url
                            } else {
                                "https://" + component.url
                            }

                            // Launch in browser-in-app
                            customTabsIntent.launchUrl(context,
                                Uri.parse(url))
                        }
                    )
                }

                RichText.Partition.Type.Navi ->
                {
                    NaviReferenceChip(
                        refNavi = component.navi!!,
                        paddingL = 1.dp,
                        paddingR = 1.dp,
                        onClick = naviClick,
                        type = component.naviType
                    )
                }

                RichText.Partition.Type.Space ->
                {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaviReferenceChip(
    refNavi: String,
    type: String?,
    paddingL: Dp = 0.dp, paddingR: Dp = 0.dp,
    onClick: (String) -> Unit,
) {
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
                text = Navi.lemmaForm(refNavi, type),
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = lintSpecialText(type)
            )
        },
        modifier = Modifier
            .defaultMinSize(minHeight = 0.dp),
    )
    Spacer(modifier = Modifier
        .padding(horizontal = paddingR)
        .defaultMinSize(minHeight = 0.dp))
}


//endregion

@Composable
fun AffixTable(
    affixes: List<AffixListElement>?,
    style: TextStyle = Typography.bodyLarge,
    language: Language,
    show: Boolean,
    naviClick: (String) -> Unit
) {
    if (affixes.isNullOrEmpty()) {
        return
    }

    //Collapsable card
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.animateContentSize()) {
        if (show) {
            Spacer(Modifier.padding(6.dp))
            Card(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp)
                ) {
                    //Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = "AFFIXES",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { expanded = !expanded }) {
                            if (expanded) {
                                Icon(Icons.Filled.KeyboardArrowDown, "fold affixes")
                            } else {
                                Icon(Icons.Filled.KeyboardArrowUp, "expand affixes")
                            }
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(Modifier.padding(end = 10.dp)) {

                            for (affix in affixes) {

                                LazyRow(
                                    Modifier.padding(bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item {

                                        //Affix+type
                                        if (affix.ref) {
                                            NaviReferenceChip(
                                                refNavi = affix.affix,
                                                type = affix.type,
                                                onClick = naviClick
                                            )
                                        } else {
                                            Text(
                                                text = Navi.lemmaForm(affix.affix, affix.type),
                                                style = style,
                                                color = lintSpecialText(affix.type)
                                            )
                                        }

                                        //Type
                                        Spacer(modifier = Modifier.padding(2.dp))

                                        var showTypeInfo by remember { mutableStateOf(false) }

                                        Card(
                                            onClick = { showTypeInfo = !showTypeInfo },
                                            colors = CardDefaults.elevatedCardColors()
                                        )
                                        {
                                            Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                                                AnimatedContent(
                                                    targetState = showTypeInfo,
                                                    label = "word type"
                                                )
                                                { showDetails ->

                                                    if (showDetails) {
                                                        Text(
                                                            text = stringResource(
                                                                id = Navi.typeDetails(
                                                                    affix.type
                                                                )
                                                            ),
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                            style = MaterialTheme.typography.displaySmall
                                                                .copy(fontSize = 15.sp),
                                                            color = lintSpecialText(affix.type)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = Navi.typeDisplay(affix.type),
                                                            style = MaterialTheme.typography.displaySmall
                                                                .copy(fontSize = 15.sp),
                                                            color = lintSpecialText(affix.type)
                                                        )
                                                    }

                                                }
                                            }
                                        }


                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(horizontal = 10.dp))

                                        //Components
                                        if (affix.components != null) {
                                            RichTextComponent(
                                                richText = affix.components,
                                                language = language,
                                                naviClick = naviClick
                                            )
                                        }
                                    }

                                    item {

                                        Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                                        //meaning
                                        RichTextComponent(
                                            richText = affix.meaning,
                                            language = language,
                                            naviClick = naviClick
                                        )

                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.padding(6.dp))

                            }
                        }
                        Spacer(Modifier.padding(3.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SourcesCard(
    sources: List<Source>?,
    style: TextStyle = Typography.bodyLarge,
    show: Boolean,
    naviClick: (String) -> Unit
) {
    if (sources.isNullOrEmpty()) {
        return
    }

    //Collapsable card
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.animateContentSize()) {
        if (show) {
            Spacer(Modifier.padding(6.dp))
            Card(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp)
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
                        Column {
                            for ((index, source) in sources.withIndex()) {
                                // "either a string describing the source,
                                // or an array containing a description and an URL." (navi-tsim)
                                Column(
                                    Modifier.padding(end = 10.dp)
                                ) {
                                    Row(
                                        Modifier.padding(bottom = 2.dp)
                                    ) {
                                        //Index at the beginnning of source
                                        Text(
                                            "${index + 1}.",
                                            style = style.copy(fontWeight = FontWeight.Bold)
                                        )

                                        when (source.type) {
                                            Source.Type.RichURL -> {
                                                // URL
                                                RichTextComponent(
                                                    naviClick = { /* UNUSED */ },
                                                    richText = source.richUrl,
                                                    language = Language.English, // UNUSED?
                                                    padding = false
                                                )
                                            }

                                            Source.Type.RichText -> {
                                                // Rich Text
                                                Column {
                                                    for (entry in source.richText!!) {
                                                        RichTextComponent(
                                                            richText = entry,
                                                            naviClick = naviClick,
                                                            language = Language.English, //UNUSED?
                                                            padding = false
                                                        )
                                                    }
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
    }
}




@Preview
@Composable
fun NaviCardPreview() {
    val naviList = listOf<DictNavi>(
        DictNavi(
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
            meaning_note = "Used together with [zun:conj]. Check out reykunyu.lu and [skxawng:n]!",
            seeAlso = listOf("oeng:pn", "oe:pn"),
            status = "unconfirmed",
            status_note = "Not yet officially confirmed by Pawl.",
            image = "toruk.png",
        )
    )

    val debugJson = "{\"fromNa'vi\":[{\"tìpawm\":\"tsamesuteru\",\"sì'eyng\":[{\"na'vi\":\"tute\",\"type\":\"n\",\"pronunciation\":[{\"syllables\":\"tu-te\",\"stressed\":1,\"audio\":[{\"speaker\":\"Plumps\",\"file\":\"plumps/tute:n.mp3\"},{\"speaker\":\"tsyili\",\"file\":\"tsyili/tute:n.mp3\"}],\"ipa\":{\"FN\":\"[ˈtu.tɛ]\",\"RN\":\"[ˈtu.tɛ]\"}}],\"translations\":[{\"en\":\"person\",\"de\":\"Person, Mensch (Na'vi)\",\"et\":\"isik\",\"fr\":\"personne\",\"hu\":\"személy\",\"nl\":\"persoon\",\"pl\":\"osoba\",\"ru\":\"персона (на'ви или человек)\",\"sv\":\"person\",\"xnavi\":\"’awa rusey a lu kanu nìftxan kuma po tsun fpivìl snoteri, nìpxi hapxì Na'viyä fu tawtute\"}],\"source\":[[\"http://en.wiktionary.org/wiki/Appendix:Na'vi\",\"\",\"\"]],\"conjugated\":[{\"type\":\"n\",\"conjugation\":{\"result\":[\"tsamesuteru\",\"tsamesuter\"],\"root\":\"tute\",\"affixes\":[\"tsa\",\"me\",\"\",\"\",\"\",\"r\",\"\"]}}],\"affixes\":[{\"type\":\"prefix\",\"affix\":{\"na'vi\":\"tsa\",\"type\":\"aff:pre\",\"translations\":[{\"en\":\"that\",\"de\":\"jener, jene, jenes\",\"nl\":\"die, dat\",\"fr\":\"ce/cet/cette ...-là\"}],\"source\":[[\"Horen Lì'fyayä LeNa'vi (§3.3.2)\",\"https://files.learnnavi.org/docs/horen-lenavi.pdf\"]]}},{\"type\":\"prefix\",\"affix\":{\"na'vi\":\"me\",\"type\":\"aff:pre\",\"translations\":[{\"en\":\"(dual)\",\"de\":\"(Dual, zwei von etwas)\",\"nl\":\"(tweevoud)\",\"fr\":\"(duel, deux de quelque chose)\"}],\"etymology\":\"Likely related to [mune:num].\",\"source\":[[\"Horen Lì'fyayä LeNa'vi (§3.1.4)\",\"https://files.learnnavi.org/docs/horen-lenavi.pdf\"]]}},{\"type\":\"suffix\",\"affix\":{\"na'vi\":\"r\",\"type\":\"aff:suf\",\"translations\":[{\"en\":\"(dative case: indirect object)\",\"fr\":\"(cas du datif, complément indirect)\",\"nl\":\"(datief, meewerkend voorwerp)\",\"de\":\"(Dativ-Fallendung, indirektes Objekt)\"}],\"source\":[[\"Horen Lì'fyayä LeNa'vi (§3.1.1)\",\"https://files.learnnavi.org/docs/horen-lenavi.pdf\",\"\"]]}}],\"conjugation\":{\"forms\":[[\"-tute-\",\"-tute-l\",\"-tute-t(i)\",\"-tute-r(u)\",\"-tute-yä\",\"-tute-ri\"],[\"me-{s}ute-\",\"me-{s}ute-l\",\"me-{s}ute-t(i)\",\"me-{s}ute-r(u)\",\"me-{s}ute-yä\",\"me-{s}ute-ri\"],[\"pxe-{s}ute-\",\"pxe-{s}ute-l\",\"pxe-{s}ute-t(i)\",\"pxe-{s}ute-r(u)\",\"pxe-{s}ute-yä\",\"pxe-{s}ute-ri\"],[\"(ay)-{s}ute-\",\"(ay)-{s}ute-l\",\"(ay)-{s}ute-t(i)\",\"(ay)-{s}ute-r(u)\",\"(ay)-{s}ute-yä\",\"(ay)-{s}ute-ri\"]]},\"derived\":[{\"na'vi\":\"aysupe\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche Personen?\",\"en\":\"what people\",\"fr\":\"quelles personnes?\",\"nl\":\"welke personen\"}]},{\"na'vi\":\"matu\",\"type\":\"intj\",\"translations\":[{\"en\":\"excuse me, hey\",\"nl\":\"sorry (om iemands aandacht te vragen)\",\"de\":\"Entschuldigung (um jemandes Aufmerksamkeit zu bekommen)\",\"fr\":\"pardonnez-moi, hé\"}]},{\"na'vi\":\"mesupe\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche beiden Personen?\",\"en\":\"what two people\",\"fr\":\"quelles deux personnes?\",\"nl\":\"welke twee personen\"}]},{\"na'vi\":\"paysu\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche Personen?\",\"en\":\"what people\",\"fr\":\"quelles personnes?\",\"nl\":\"welke personen\"}]},{\"na'vi\":\"pemsu\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche beiden Personen?\",\"en\":\"what two people\",\"fr\":\"quelles deux personnes?\",\"nl\":\"welke twee personen\"}]},{\"na'vi\":\"pepsu\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche drei Personen?\",\"en\":\"what three people\",\"fr\":\"quelles trois personnes?\",\"nl\":\"welke drie personen\"}]},{\"na'vi\":\"pesu\",\"type\":\"inter\",\"translations\":[{\"de\":\"Wer? (welcher Person?)\",\"en\":\"who\",\"et\":\"kes\",\"fr\":\"qui\",\"hu\":\"ki(csoda)?\",\"nl\":\"wie\",\"pl\":\"kto\",\"ru\":\"кто\",\"sv\":\"vem\"}]},{\"na'vi\":\"pxesupe\",\"type\":\"inter\",\"translations\":[{\"de\":\"Welche drei Personen?\",\"en\":\"what three people\",\"fr\":\"quelles trois personnes?\",\"nl\":\"welke drie personen\"}]},{\"na'vi\":\"syeptute\",\"type\":\"n\",\"translations\":[{\"de\":\"Hyneman (Cobralilien ähnlich), Menschenfalle, pandoratonia myopora\",\"en\":\"hyneman, person trapper\",\"et\":\"Lõkstaim (hyneman)\",\"fr\":\"[Pandoratonia myopora]\",\"hu\":\"hyneman, emberfogó (pandorai növényfaj)\",\"nl\":\"hyneman, mensen vanger\",\"pl\":\"łapiący w sidła\",\"ru\":\"hyneman, растениеловушка\",\"sv\":\"personfångare\"}]},{\"na'vi\":\"tawtute\",\"type\":\"n\",\"translations\":[{\"en\":\"Skyperson, human\",\"sv\":\"Himmelsperson, människa\",\"ru\":\"землянин, «небесная персона»\",\"pl\":\"Człowiek nieba, człowiek\",\"nl\":\"Luchtpersoon, mens\",\"hu\":\"ég ember (ember)\",\"fr\":\"Humain | Personne du ciel | Celui qui vient du ciel\",\"et\":\"Taevaisik, inimene\",\"de\":\"Himmelsperson, Mensch\"}]},{\"na'vi\":\"tupe\",\"type\":\"inter\",\"translations\":[{\"de\":\"Wer? (welche Person?)\",\"en\":\"who\",\"et\":\"kes\",\"fr\":\"qui?\",\"hu\":\"ki(csoda)?\",\"nl\":\"wie\",\"pl\":\"kto\",\"ru\":\"кто, который\",\"sv\":\"vem\"}]},{\"na'vi\":\"tutan\",\"type\":\"n\",\"translations\":[{\"de\":\"Mann, männliche Person\",\"en\":\"man, male (person)\",\"et\":\"mees\",\"fr\":\"mâle (personne) | homme | mec\",\"hu\":\"férfi\",\"nl\":\"man (persoon)\",\"pl\":\"mężczyzna\",\"ru\":\"мужчина\",\"sv\":\"manlig person\",\"x-navi\":\"tute afnelan\"}]},{\"na'vi\":\"tuté\",\"type\":\"n\",\"translations\":[{\"de\":\"Frau, weibliche Person\",\"en\":\"woman, female (person)\",\"et\":\"naine (isik)\",\"fr\":\"femme\",\"hu\":\"nő\",\"nl\":\"vrouw (persoon)\",\"pl\":\"kobieta\",\"ru\":\"женщина\",\"sv\":\"kvinna (person)\",\"x-navi\":\"tute afnele\"}]},{\"na'vi\":\"tuteo\",\"type\":\"pn\",\"translations\":[{\"de\":\"jemand, irgendjemand\",\"en\":\"somebody, someone\",\"et\":\"keegi\",\"fr\":\"quelqu'un\",\"hu\":\"valaki\",\"nl\":\"iemand\",\"pl\":\"ktoś\",\"ru\":\"ктото\",\"sv\":\"någon\"}]},{\"na'vi\":\"tutsena\",\"type\":\"n\",\"translations\":[{\"de\":\"Trage, Liege\",\"en\":\"stretcher\",\"et\":\"kanderaam (ese, millega filmis Grace'i lohistati)\",\"fr\":\"civière\",\"hu\":\"hordány\",\"nl\":\"brancard\",\"ru\":\"носилки\",\"x-navi\":\"sä’o a fko sar fte hivena tuteti\"}]}],\"sentences\":[{\"na'vi\":[[\"Fayupxaremì\",[\"'upxare:n\"]],[\"oe\",[\"oe:pn\"]],[\"payängkxo\",[\"pängkxo:v:in\"]],[\"teri\",[\"teri:adp\"]],[\"horen\",[\"koren:n\"]],[\"lì'fyayä\",[\"lì'fya:n\"]],[\"leNa'vi\",[\"leNa'vi:adj\"]],[\"fpi\",[\"fpi:adp:len\"]],[\"sute\",[\"tute:n\"]],[\"a\",[\"a:part\"]],[\"tsun\",[\"tsun:v:m\"]],[\"srekrr\",[\"srekrr:adv\"]],[\"tsat\",[\"tsa'u:pn\"]],[\"sivar.\",[\"sar:v:tr\"]]],\"translations\":{\"en\":{\"translation\":[\"In\",\"these\",\"messages,\",\"I\",\"will\",\"chat\",\"about\",\"the\",\"rules\",\"of\",\"the\",\"Na'vi\",\"language\",\"for\",\"people\",\"that\",\"can\",\"use\",\"it\",\"already.\"],\"mapping\":[[1,2,3],[4],[5,6],[7],[8,9],[10,11,13],[12],[14],[15],[16],[17],[20],[19],[18]]}},\"source\":[\"Na'viteri: “Zola’u nìprrte’! Welcome!”\",\"https://naviteri.org/2010/06/first-post/\",\"2010-06-24\"]}]}],\"aysämok\":[]}],\"toNa'vi\":[]}"

    val converted = OnlineTranslateSearch().convertTranslationResult(debugJson).fromNavi[0].second[0]

    LazyColumn {
        items(items = naviList) { item ->
            var expanded by remember {
                mutableStateOf(true)
            }
            NaviCard(
                item.toNavi().copy(
                    conjugatedExplanation = converted.conjugatedExplanation,
                    affixes = converted.affixes
                ),
                Language.English, {}, expanded, { expanded = !expanded }
            )
        }
    }
}