@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kip.reykunyu.R
import com.kip.reykunyu.data.dict.Navi
import com.kip.reykunyu.data.dict.SearchType
import com.kip.reykunyu.ui.NaviCard
import com.kip.reykunyu.viewmodels.DictSearchState
import com.kip.reykunyu.viewmodels.DictionarySearchViewModel
import com.kip.reykunyu.viewmodels.OfflineDictState
import com.kip.reykunyu.viewmodels.OfflineDictionaryViewModel
import kotlinx.coroutines.launch


//The class for the main dictionary UI
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun DictionaryScreen(
    offlineDictViewModel: OfflineDictionaryViewModel = viewModel(),
    searchViewModel: DictionarySearchViewModel = viewModel(),
    openNavDrawerAction: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val dictState = offlineDictViewModel.offlineDictState

    val onSearch = {
        focusManager.clearFocus()
        searchViewModel.search()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    DictionarySearchBar(
                        searchString = searchViewModel.searchInput,
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
                    SearchTypeIcon(searchViewModel)
                }
            )
        },

        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(paddingValues)
            ) {

                when (dictState) {
                    is OfflineDictState.NotLoaded -> DownloadDictionaryButton {
                        offlineDictViewModel.downloadDictionary()
                    }
                    is OfflineDictState.Loading -> {
                        LoadingView(
                            text = stringResource(R.string.downloading_dict)
                        )
                    }
                    is OfflineDictState.Loaded -> {
                        AnimatedContent(targetState = searchViewModel.dictSearchState)
                        { state ->
                            when (state) {
                                is DictSearchState.Success -> {
                                    SearchDisplay(
                                        fromNavi = state.result.fromNavi,
                                        toNavi = state.result.toNavi,
                                        naviAction = {
                                            Log.i("REYKUNYU", "NAVI REF: $it")
                                            searchViewModel.updateSearchInput(it)
                                            onSearch()
                                        }
                                    )
                                }
                                DictSearchState.Error -> IconInfoView(text = stringResource(R.string.error))
                                DictSearchState.Loading -> LoadingView(text = stringResource(id = R.string.loading))
                                DictSearchState.Standby -> IconInfoView(
                                    text = stringResource(id = R.string.search_help)
                                ) {
                                    Icon(Icons.Rounded.Search, null, modifier = it)
                                }
                            }
                        }

                    }
                    is OfflineDictState.Error -> {
                        IconInfoView(text = stringResource(R.string.error))
                    }
                }
            }
        }
    )


}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SearchTypeIcon(searchViewModel: DictionarySearchViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val selectAction = { o: SearchType ->
        searchViewModel.updateSearchType(o)
        expanded = false
    }

    Column {

        IconButton(onClick = { expanded = !expanded }) {
            Crossfade(targetState = searchViewModel.searchType) {
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
                type = SearchType.Translate,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchType.Sentence,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchType.Annotated,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            SearchTypeMenuItem(
                type = SearchType.Rhymes,
                onlineOnly = true,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

            Divider()

            SearchTypeMenuItem(
                type = SearchType.Offline,
                onlineOnly = false,
                searchViewModel = searchViewModel,
                selectAction = selectAction
            )

        }
    }
}

@Composable
fun SearchTypeMenuItem(type: SearchType, onlineOnly: Boolean,
                   searchViewModel: DictionarySearchViewModel, selectAction: (SearchType) -> Unit
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
        modifier = if(searchViewModel.searchType == type)
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
    onInputChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    //Search bar
    OutlinedTextField(
        value = searchString,
        onValueChange = onInputChanged,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        placeholder =
        {
            Text(
                text = stringResource(R.string.search_box),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            )
        },
        trailingIcon =
        {
            IconButton(onClick = onSearch ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_description),
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(32.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchDisplay(fromNavi: List<Pair<String, List<Navi>>>, toNavi: List<Navi>, naviAction: (String) -> Unit) {

    // 2 Pages: From Na'vi words and [Language] to Na'vi words
    val initPage = if (fromNavi.isEmpty() && toNavi.isNotEmpty()) { 1 } else { 0 }

    val state = rememberPagerState(initialPage = initPage)

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
            HorizontalPager(pageCount = 2, state = state, pageSpacing = 10.dp,
                userScrollEnabled = fromNavi.size <= 1
            ) { o ->
                when (o) {
                    0 -> FromNaviList(
                        fromNavi = fromNavi,
                        naviAction = { naviAction(it) }
                    )
                    1 -> NaviList(naviList = toNavi, naviAction = { naviAction(it) })
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FromNaviList(
    fromNavi: List<Pair<String, List<Navi>>>,
    naviAction: (String) -> Unit
) {

    if(fromNavi.size <= 1){ //Auto hide selection bar if only one element
        NaviList(naviList = fromNavi[0].second, naviAction = naviAction)
        return
    }

    val state = rememberPagerState(initialPage = 0) //chosen word index
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

        Divider(Modifier
            .fillMaxWidth()
        )

        HorizontalPager(pageCount = fromNavi.size, state=state) {
            NaviList(naviList = fromNavi[it].second, naviAction = naviAction)
        }

    }
}

@Composable
fun NaviList(naviList: List<Navi>, naviAction: (String) -> Unit) {

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
