@file:OptIn(ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kip.reykunyu.R
import com.kip.reykunyu.data.api.AudioImageRepository
import com.kip.reykunyu.data.dict.*
import com.kip.reykunyu.data.offline.DictNavi
import com.kip.reykunyu.ui.theme.Typography
import com.valentinilk.shimmer.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun NaviCard(navi: Navi, language: Language, naviClick: (String) -> Unit, expanded: Boolean, toggleExpand: () -> Unit) {

    val expandable = !navi.seeAlso.isNullOrEmpty() || !navi.pronunciation.isNullOrEmpty() ||
                navi.status_note != null || !navi.status.isNullOrEmpty() ||
                !navi.meaning_note.isNullOrEmpty() || navi.etymology != null ||
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
            Divider()
            Spacer(Modifier.padding(6.dp))

            Text(
                text = "TRANSLATIONS",
                style = labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            var i = 1
            for (translation in navi.translations) {
                //Displays the proper language
                if (translation[language] != null) {
                    if (i != 1)
                        Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        text = translation[language]!!,
                        style = Typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                    )
                    i++
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    //meaning note
                    if (navi.meaning_note != null) {
                        for (note in navi.meaning_note){
                            RichTextComponent(richText = note, naviClick = naviClick)
                        }

                    }

                    AutoSpacer(navi.translations, navi.meaning_note, 5.dp, divider = false)


                    //etymology
                    RichInfoModule(
                        category = "ETYMOLOGY",
                        richText = navi.etymology,
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
                                    paddingR = 10.dp
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
                                    paddingR = 10.dp
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
                        )
                    )

                    //statusNote
                    RichInfoModule(
                        category = "NOTE", richText = navi.status_note, padding = 3.dp,
                        naviClick = naviClick
                    )


                }
            }

            SourcesCard(sources = navi.source, show = expanded, naviClick = naviClick)


            Spacer(Modifier.padding(6.dp))


            //EXPAND AND FOLD BUTTON
            if(expandable){
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
                Toast.makeText(context, "Failed to play audio!", Toast.LENGTH_SHORT).show()
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
            Divider(modifier = Modifier.padding(horizontal = 20.dp))
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
        naviClick = naviClick
    )
}

@Composable
fun TextModule(
    category: String,
    content: String?,
    style: TextStyle = Typography.bodyLarge,
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
        naviClick = {}
    )
}


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
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RichTextComponent(
    richText: RichText?,
    style: TextStyle = Typography.bodyLarge,
    padding: Boolean = true,
    naviClick: (String) -> Unit
) {
    if(richText == null) {
        return
    }


    //URL
    val context = LocalContext.current

    FlowRow(
        modifier = if (padding) Modifier.padding(horizontal = 20.dp) else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Rich Text
        for (component in richText.sequence) {
            when (component.type) {

                RichText.Partition.Type.Text ->
                {
                    //Hacky way of supporting line breaks
                    if (component.text!! == "\n"){
                        Spacer(Modifier.padding(horizontal = 100000.dp, vertical=2.dp))
                    }else {
                        Text(
                            text = component.text,
                            style = style
                        )
                    }
                }

                RichText.Partition.Type.Url ->
                {
                    ClickableText(
                        text = component.urlDisplay!!,
                        style = style.copy(
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        onClick = naviClick
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
    paddingL: Dp = 0.dp, paddingR: Dp = 0.dp,
    onClick: (String) -> Unit
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




//endregion

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
            meaning_note = null/*"Used together with [zun:conj]. Check out reykunyu.lu and [skxawng:n]!"*/,
            seeAlso = listOf("oeng:pn", "oe:pn"),
            status = "unconfirmed",
            status_note = "Not yet officially confirmed by Pawl.",
            image = "toruk.png"
        )
    )
    LazyColumn {
        items(items = naviList) { item ->
            var expanded by remember {
                mutableStateOf(false)
            }
            NaviCard(item.toNavi(), Language.English, {}, expanded, {expanded = !expanded})
        }
    }
}