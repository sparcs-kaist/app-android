package org.sparcs.soap.app.features.postList

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.app.shared.views.contentViews.MaterialSearchField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.postList.components.postList.PostList
import org.sparcs.soap.app.features.postList.components.postListRow.BoardNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewPostListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListView(
    viewModel: PostListViewModelProtocol = hiltViewModel<PostListViewModel>(),
    navController: NavController,
) {
    var loadedInitialPost by rememberSaveable { mutableStateOf(false) }
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    // Open whenever a search is active, so a keyword never filters the list out of sight.
    var showSearchBar by rememberSaveable { mutableStateOf(viewModel.searchKeyword.value.isNotEmpty()) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val board = viewModel.board

    val isPreview = LocalInspectionMode.current
    val backStackEntry = if (!isPreview) navController.currentBackStackEntry else null

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.lastClickedPostId?.let { id ->
                viewModel.refreshItem(id)
                viewModel.lastClickedPostId = null
            }
            val needsRefresh =
                navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("listNeedsRefresh")
                    ?: false
            if (needsRefresh) {
                viewModel.fetchInitialPosts()
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "listNeedsRefresh",
                    false
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        val json = Gson().toJson(board)
        backStackEntry?.savedStateHandle?.set("board_json", json)
        if (!loadedInitialPost) {
            loadedInitialPost = true
            viewModel.board = board
            viewModel.bind()
        }
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()
    val listState = rememberLazyListState()

    // Closing search also clears it, so the list is never filtered by a hidden keyword.
    val closeSearch = {
        showSearchBar = false
        viewModel.onSearchTextChange("")
    }
    BackHandler(enabled = showSearchBar, onBack = closeSearch)

    Scaffold(
        topBar = {
            BoardNavigationBar(
                title = board.name.localized(),
                subTitle = board.group.name.localized(),
                onClickSearch = { if (showSearchBar) closeSearch() else showSearchBar = true },
                isSelected = showSearchBar,
                navController = navController,
                scrollBehavior = topBarScrollBehavior
            )
        },
        floatingActionButton = {
            if (!board.isReadOnly && board.userWritable == true) {
                // Shows its label at the top of the list and shrinks to the icon once scrolled.
                val expanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
                val writeLabel = stringResource(R.string.write)
                ExtendedFloatingActionButton(
                    onClick = {
                        val json = Uri.encode(Gson().toJson(board))
                        navController.navigate(Channel.PostCompose.name + "?board_json=$json")
                    },
                    expanded = expanded,
                    icon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = if (expanded) null else writeLabel
                        )
                    },
                    text = { Text(writeLabel) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(name = "Ara Post List")
    ) { innerPadding ->
        // Edge to edge: the list scrolls behind the gesture bar, with room at the end to clear it
        // and the Write button.
        val listPadding = PaddingValues(
            top = 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 88.dp
        )
        val rowPadding = PaddingValues(horizontal = 16.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showSearchBar) {
                    MaterialSearchField(
                        query = searchKeyword,
                        onQueryChange = { viewModel.onSearchTextChange(it) },
                        placeholder = stringResource(R.string.search),
                        autoFocus = true,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (state) {
                        is PostListViewModel.ViewState.Loading -> PostList(
                            posts = null,
                            onRefresh = {},
                            onLoadMore = {},
                            onPostClick = {},
                            isRefreshing = false,
                            contentPadding = listPadding,
                            rowPadding = rowPadding
                        )
                        is PostListViewModel.ViewState.Loaded -> {
                            val loadedState = state as PostListViewModel.ViewState.Loaded
                            PostList(
                                posts = loadedState.posts,
                                onLoadMore = { scope.launch { viewModel.loadNextPage() } },
                                onRefresh = {
                                    isRefreshing = true
                                    scope.launch {
                                        viewModel.fetchInitialPosts()
                                        delay(500)
                                        isRefreshing = false
                                    }
                                },
                                onPostClick = { post ->
                                    viewModel.lastClickedPostId = post.id
                                    navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                                },
                                isRefreshing = isRefreshing,
                                keyword = searchKeyword,
                                contentPadding = listPadding,
                                listState = listState,
                                rowPadding = rowPadding,
                                isLoadingMore = viewModel.isLoadingMore
                            )
                        }

                        is PostListViewModel.ViewState.Error -> {
                            val errorState = state as PostListViewModel.ViewState.Error
                            ErrorView(
                                error = errorState.error,
                                onRetry = {
                                    scope.launch { viewModel.fetchInitialPosts() }
                                    viewModel.bind()
                                }
                            )
                        }
                    }

                    if (searchKeyword.isNotEmpty() && viewModel.posts.isEmpty() && state is PostListViewModel.ViewState.Loaded) {
                        EmptyView(
                            searchText = searchKeyword,
                            onClear = {
                                viewModel.onSearchTextChange("")
                                scope.launch { viewModel.fetchInitialPosts() }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyView(
    searchText: String,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.no_results_found_for, "\"$searchText\""),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onClear) {
            Text(stringResource(R.string.clear_search_text))
        }
    }
}

// Mark: Preview
@Preview(name = "Loading State", showBackground = true)
@Composable
private fun PreviewPostListLoading() {
    val viewModel = PreviewPostListViewModel(
        initialState = PostListViewModel.ViewState.Loading,
        board = AraBoard.mock()
    )
    Theme { PostListView(viewModel = viewModel, rememberNavController()) }
}

@Preview(name = "Loaded State", showBackground = true)
@Composable
private fun PreviewPostListLoaded() {
    val viewModel = PreviewPostListViewModel(
        initialState = PostListViewModel.ViewState.Loaded(AraPost.mockList()),
        board = AraBoard.mock(),
        posts = AraPost.mockList()
    )
    Theme { PostListView(viewModel = viewModel, rememberNavController()) }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun PreviewPostListError() {
    val viewModel = PreviewPostListViewModel(
        initialState = PostListViewModel.ViewState.Error(Exception()),
        board = AraBoard.mock()
    )
    Theme { PostListView(viewModel = viewModel, rememberNavController()) }
}

@Preview(name = "Empty Board", showBackground = true)
@Composable
private fun PreviewPostListEmptyBoard() {
    val viewModel = PreviewPostListViewModel(
        initialState = PostListViewModel.ViewState.Loaded(emptyList()),
        board = AraBoard.mock(),
        posts = emptyList()
    )
    Theme { PostListView(viewModel = viewModel, rememberNavController()) }
}

@Preview(name = "Empty Search", showBackground = true)
@Composable
private fun PreviewPostListEmptySearch() {
    val viewModel = PreviewPostListViewModel(
        initialState = PostListViewModel.ViewState.Loaded(emptyList()),
        board = AraBoard.mock(),
        posts = emptyList(),
        initialSearchKeyword = "no results"
    )
    Theme { PostListView(viewModel = viewModel, rememberNavController()) }
}