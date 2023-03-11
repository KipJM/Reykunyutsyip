@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kip.reykunyu.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
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
    searchViewModel: DictionarySearchViewModel = viewModel()
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
                    IconButton(onClick = { TODO() }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Reykunyu sidebar menu access"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearch ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_description),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
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
                text = stringResource(R.string.search),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            )
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
fun SearchDisplay(fromNavi: List<Navi>, toNavi: List<Navi>, naviAction: (String) -> Unit) {

    // 2 Pages: From Na'vi words and [Language] to Na'vi words
    val initPage = if (fromNavi.isEmpty() && toNavi.isNotEmpty()) { 1 } else { 0 }

    val state = rememberPagerState(initialPage = initPage)
    val titles = listOf("Na\'vi to Lang (${fromNavi.size})", "Lang to Na\'vi (${toNavi.size})")

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

        HorizontalPager(pageCount = 2, state = state, pageSpacing = 1.dp) {
            when (state.currentPage) {
                0 -> NaviList(naviList = fromNavi, naviAction = { naviAction(it) })
                1 -> NaviList(naviList = toNavi, naviAction = { naviAction(it) })
            }
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