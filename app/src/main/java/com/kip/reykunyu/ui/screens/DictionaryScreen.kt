@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.Language
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.SearchMode
import com.kip.reykunyu.ui.components.NaviCard
import com.kip.reykunyu.viewmodels.DictionarySearchViewModel
import com.kip.reykunyu.viewmodels.OfflineDictState
import com.kip.reykunyu.viewmodels.OfflineDictionaryViewModel
import com.kip.reykunyu.viewmodels.PreferenceViewModel
import com.kip.reykunyu.viewmodels.SearchState
import kotlinx.coroutines.launch


//The class for the main dictionary UI
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun DictionaryScreen(
    offlineDictViewModel: OfflineDictionaryViewModel = viewModel(factory = OfflineDictionaryViewModel.Factory), // For @Preview
    searchViewModel: DictionarySearchViewModel = viewModel(), // For @Preview
    preferenceViewModel: PreferenceViewModel = viewModel(factory = PreferenceViewModel.Factory), // For @Preview
    openNavDrawerAction: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val dictState = offlineDictViewModel.offlineDictState
    val preferenceState = preferenceViewModel.preferenceState.collectAsState().value

    val onSearch = {
        focusManager.clearFocus()
        searchViewModel.search(preferenceState.searchLanguage)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    DictionarySearchBar(
                        searchString = searchViewModel.searchInput,
                        enabled = (dictState == OfflineDictState.Loaded),
                        onInputChanged = {searchViewModel.updateSearchInput(it)},
                        onSearch = onSearch
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openNavDrawerAction) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Reykunyu sidebar menu access"
                        )
                    }
                },
                actions = {
                    SearchTypeIcon(searchViewModel, enabled = (dictState == OfflineDictState.Loaded))
                }
            )
        },

        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(paddingValues)
            ) {

                when (dictState) {
                    //Auto load dictionary on start
                    is OfflineDictState.NotLoaded -> offlineDictViewModel.downloadDictionary()
                    /*DownloadDictionaryButton {
                        offlineDictViewModel.downloadDictionary()
                    }*/
                    is OfflineDictState.Loading -> {
                        LoadingView(
                            text = stringResource(R.string.downloading_dict)
                        )
                    }
                    is OfflineDictState.Loaded -> {
                        AnimatedContent(targetState = searchViewModel.searchState, label = "ContentState")
                        { state ->
                            when (state) {

                                is SearchState.Standby -> IconInfoView(
                                    text = stringResource(id = R.string.search_help)
                                ) {
                                    Icon(Icons.Rounded.Search, null, modifier = it)
                                }


                                SearchState.Loading -> LoadingView(text = stringResource(id = R.string.loading))


                                is SearchState.TranslateSuccess -> {
                                    SearchDisplay(
                                        fromNavi = state.result.fromNavi,
                                        toNavi = state.result.toNavi,
                                        language = preferenceState.searchLanguage,
                                        naviAction = {
                                            Log.i("REYKUNYU", "NAVI REF: $it")
                                            searchViewModel.updateSearchInput(it)
                                            onSearch()
                                        }
                                    )
                                }

                                is SearchState.AnnotatedSuccess -> TODO()
                                is SearchState.RhymesSuccess -> TODO()
                                is SearchState.SentenceSuccess -> TODO()


                                is SearchState.Error -> {
                                    var text = stringResource(R.string.error) + "(${state.info})"
                                    var icon = painterResource(R.drawable.baseline_error_24)

                                    when (state.id){
                                        "COMING_SOON" -> {
                                            text = state.info ?: "Coming soon!"
                                            icon = painterResource(id = R.drawable.rhymes_24)
                                        }
                                    }
                                    IconInfoView(
                                        text = text, icon = {Icon(icon, null, modifier = it)}
                                    )
                                }
                            }
                        }

                    }
                    is OfflineDictState.Error -> {
                        IconInfoView(text = stringResource(R.string.dictionary_error) + " (${dictState.message})")
                    }
                }
            }
        }
    )


}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SearchTypeIcon(searchViewModel: DictionarySearchViewModel, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val selectAction = { o: SearchMode ->
        searchViewModel.updateSearchType(o)
        expanded = false
    }

    Column {

        IconButton(onClick = { expanded = !expanded }, enabled = enabled) {
            Crossfade(targetState = searchViewModel.searchMode, label = "SearchMode") {
                Icon(
                    painterResource(it.icon),
                    contentDescription = stringResource(it.display),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        )
        {
            SearchTypeMenuItem(
                type = SearchMode.Translate,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchMode.Sentence,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchMode.Annotated,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchMode.Rhymes,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            HorizontalDivider()

            SearchTypeMenuItem(
                type = SearchMode.Offline,
                onlineOnly = false,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

        }
    }
}

@Composable
fun SearchTypeMenuItem(type: SearchMode, onlineOnly: Boolean,
                       searchViewModel: DictionarySearchViewModel, selectAction: (SearchMode) -> Unit
) {
    DropdownMenuItem(
        enabled = !searchViewModel.offlineMode || !onlineOnly,
        text = { Text(stringResource(type.display)) },
        leadingIcon =
        {
            Icon(
                painterResource(type.icon),
                stringResource(type.display)
            )
        },
        onClick = { selectAction(type) },
        modifier = if(searchViewModel.searchMode == type)
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        else Modifier
    )
}


@Composable
fun DownloadDictionaryButton(
    clickAction: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = clickAction,
            ) {
            Text(
                text = "Download Dictionary :)",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun LoadingView(text: String) {
    IconInfoView(
        text = text
    ) {
        CircularProgressIndicator(
            strokeWidth = 13.dp,
            strokeCap = StrokeCap.Round,
            modifier = it
        )
        Spacer(Modifier.padding(15.dp))
    }
}

@Composable
fun IconInfoView(text: String, icon: (@Composable (Modifier) -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.invoke(
            Modifier
                .size(200.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 5.dp)
        )
        Spacer(Modifier.padding(60.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySearchBar(
    searchString: String,
    enabled: Boolean,
    onInputChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    //Search bar
    ProvideTextStyle(value = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)) {
        DockedSearchBar(
            query = searchString,
            onQueryChange = onInputChanged,
            onSearch = {onSearch()},
            active = true,
            onActiveChange = {},
            placeholder = {
                Text(
                    text = stringResource(R.string.search_box),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon =
            {
                IconButton(onClick = onSearch, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.search_description),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            modifier = modifier
//                .fillMaxWidth()
                .heightIn(30.dp, 60.dp)

        ) {

        }
    }

//    OutlinedTextField(
//        value = searchString,
//        enabled = enabled,
//        onValueChange = onInputChanged,
//        keyboardOptions = KeyboardOptions.Default.copy(
//            imeAction = ImeAction.Search
//        ),
//        keyboardActions = KeyboardActions(
//            onSearch = { onSearch() }
//        ),
//
//
//        textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
//        singleLine = true,
//        modifier =
//    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchDisplay(fromNavi: List<Pair<String, List<Navi>>>, toNavi: List<Navi>, language: Language, naviAction: (String) -> Unit) {

    // 2 Pages: From Na'vi words and [Language] to Na'vi words
    val initPage = if (fromNavi.isEmpty() && toNavi.isNotEmpty()) { 1 } else { 0 }

    val state = rememberPagerState(
        initialPage = initPage,
        initialPageOffsetFraction = 0f
    ) { /*Pagecount */ 2 }

    val titles = listOf(
        "Na\'vi to Lang (${fromNavi.sumOf { it.second.size }})",
        "Lang to Na\'vi (${toNavi.size})"
    )

    val coroutineScope = rememberCoroutineScope()

    Column {
        TabRow(selectedTabIndex = state.currentPage) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                              state.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        Surface{
            HorizontalPager(state = state, pageSpacing = 10.dp,
                userScrollEnabled = fromNavi.size <= 1
            ) { o ->
                when (o) {
                    0 -> FromNaviList(
                        fromNavi = fromNavi,
                        language = language,
                        naviAction = { naviAction(it) }
                    )
                    1 -> NaviList(naviList = toNavi, language = language, naviAction = { naviAction(it) })
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FromNaviList(
    fromNavi: List<Pair<String, List<Navi>>>,
    language: Language,
    naviAction: (String) -> Unit
) {

    if(fromNavi.size <= 1){ //Auto hide selection bar if only one element
        NaviList(naviList = fromNavi[0].second, language = language, naviAction = naviAction)
        return
    }

    val state = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        // provide pageCount
        fromNavi.size
    } //chosen word index
    val coroutineScope = rememberCoroutineScope()

    val indicator = @Composable { tabPositions: List<TabPosition> ->
        FancyAnimatedIndicator(tabPositions = tabPositions, selectedTabIndex = state.currentPage)
    }


    Column {
        ScrollableTabRow(
            selectedTabIndex = state.currentPage,
            indicator = indicator,
            divider = {}
        ) {
            fromNavi.forEachIndexed { index, element ->
                Tab(
                    selected = state.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            state.animateScrollToPage(index)
                        }
                    },
                    text = { Text(element.first) }
                )
            }
        }

        HorizontalDivider(
            Modifier
                .fillMaxWidth()
        )

        HorizontalPager(state = state) {
            NaviList(naviList = fromNavi[it].second, language = language, naviAction = naviAction)
        }

    }
}

@Composable
fun NaviList(naviList: List<Navi>, language:Language, naviAction: (String) -> Unit) {

    if(naviList.isEmpty()) {
        IconInfoView(text = stringResource(R.string.results_empty)) {
            Icon(Icons.Rounded.Info, null, modifier = it)
        }
        return
    }

    val expandedList = remember {
        mutableStateMapOf<Navi, Boolean>(*naviList.map { Pair(it, false) }.toTypedArray())
    }

    val state: LazyListState = rememberLazyListState()
    LazyColumn(
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.simpleVerticalScrollbar(state)
    ) {
        items(naviList) {item ->
            NaviCard(
                navi = item,
                language = language,
                naviClick = { naviAction(it) },
                expanded = expandedList[item]!!,
                toggleExpand = {expandedList[item] = !expandedList[item]!!}
            )
        }

        item{
            Text(
                text = stringResource(R.string.end_of_list),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.padding(5.dp))
        }
    }
}



fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 8.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    val color = MaterialTheme.colorScheme.tertiary

    drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}



@Composable
fun FancyAnimatedIndicator(tabPositions: List<TabPosition>, selectedTabIndex: Int) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    val transition = updateTransition(selectedTabIndex, label = "")
    val indicatorStart by transition.animateDp(
        transitionSpec = {
            // Handle directionality here, if we are moving to the right, we
            // want the right side of the indicator to move faster, if we are
            // moving to the left, we want the left side to move faster.
            if (initialState < targetState) {
                spring(dampingRatio = 1f, stiffness = 50f)
            } else {
                spring(dampingRatio = 1f, stiffness = 1000f)
            }
        }, label = ""
    ) {
        tabPositions[it].left
    }

    val indicatorEnd by transition.animateDp(
        transitionSpec = {
            // Handle directionality here, if we are moving to the right, we
            // want the right side of the indicator to move faster, if we are
            // moving to the left, we want the left side to move faster.
            if (initialState < targetState) {
                spring(dampingRatio = 1f, stiffness = 1000f)
            } else {
                spring(dampingRatio = 1f, stiffness = 50f)
            }
        }, label = ""
    ) {
        tabPositions[it].right
    }

    val indicatorColor by transition.animateColor(label = "") {
        colors[it % colors.size]
    }

    FancyIndicator(
        // Pass the current color to the indicator
        indicatorColor,
        modifier = Modifier
            // Fill up the entire TabRow, and place the indicator at the start
            .fillMaxSize()
            .wrapContentSize(align = Alignment.BottomStart)
            // Apply an offset from the start to correctly position the indicator around the tab
            .offset(x = indicatorStart)
            // Make the width of the indicator follow the animated width as we move between tabs
            .width(indicatorEnd - indicatorStart)
    )
}

@Composable
fun FancyIndicator(color: Color, modifier: Modifier = Modifier) {
    // Draws a rounded rectangular with border around the Tab, with a 5.dp padding from the edges
    // Color is passed in as a parameter [color]
    Box(
        modifier
            .padding(5.dp)
            .fillMaxSize()
            .border(BorderStroke(2.dp, color), RoundedCornerShape(5.dp))
    )
}
