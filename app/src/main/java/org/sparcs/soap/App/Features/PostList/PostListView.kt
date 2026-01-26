@file:OptIn(ExperimentalMaterial3Api::class)

package org.sparcs.soap.App.Features.PostList

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.PostList.Components.PostList.PostList
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.BoardNavigationBar
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Ara.MockPostListViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.R

@Composable
fun PostListView(
    viewModel: PostListViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    var loadedInitialPost by rememberSaveable { mutableStateOf(false) }

    val searchKeyword by viewModel.searchKeyword.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val state = viewModel.state.collectAsState().value

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

            val needsRefresh = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("listNeedsRefresh") ?: false
            if (needsRefresh) {
                viewModel.fetchInitialPosts()
                navController.currentBackStackEntry?.savedStateHandle?.set("listNeedsRefresh", false)
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

    Scaffold(
        topBar = {
            BoardNavigationBar(
                title = board.name.localized(),
                subTitle = board.group.name.localized(),
                onClickSearch = { showSearchBar = !showSearchBar },
                isSelected = showSearchBar,
                navController = navController
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (!board.isReadOnly && board.userWritable == true) {
                ComposeButton(
                    onClick = {
                        val json = Uri.encode(Gson().toJson(board))
                        navController.navigate(Channel.PostCompose.name + "?board_json=$json")
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                if (showSearchBar) {
                    SearchCustomBar(
                        value = searchKeyword,
                        onValueChange = { value ->
                            viewModel.onSearchTextChange(value)
                        },
                        onValueClear = {
                            viewModel.onSearchTextChange("")
                        },
                        placeHolder = stringResource(R.string.search)
                    )
                }
                when (state) {
                    is PostListViewModel.ViewState.Loading -> {
                        LoadingView()
                    }

                    is PostListViewModel.ViewState.Loaded -> {
                        PostList(
                            posts = state.posts,
                            onLoadMore = {
                                coroutineScope.launch {
                                    viewModel.loadNextPage()
                                }
                            },
                            onRefresh = {
                                isRefreshing = true
                                coroutineScope.launch {
                                    viewModel.fetchInitialPosts()
                                    delay(500)
                                    isRefreshing = false
                                }
                            },
                            onPostClick = { post ->
                                viewModel.lastClickedPostId = post.id
                                navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                            },
                            onPostDisappear = { postID -> viewModel.refreshItem(postID) },
                            isRefreshing = isRefreshing,
                            keyword = searchKeyword
                        )
                    }

                    is PostListViewModel.ViewState.Error -> {
                        val error = (state).message
                        ErrorView(
                            icon = Icons.Default.Warning,
                            message = error,
                            onRetry = {
                                coroutineScope.launch {
                                    viewModel.fetchInitialPosts()
                                    viewModel.bind()
                                }
                            }
                        )
                    }
                }
            }

            if (searchKeyword.isNotEmpty() && viewModel.posts.isEmpty()) {
                //keyword에 대한 결과가 없을 경우
                EmptyView(
                    searchText = searchKeyword,
                    onClear = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.onSearchTextChange("")
                            viewModel.fetchInitialPosts()
                        }
                    }
                )
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = stringResource(R.string.no_result),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.grayBB
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_result),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_results_found_for, " \"$searchText\""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onClear() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_search_text))
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        repeat(15) {
            PostListSkeletonRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
        }
    }
}


@Composable
private fun ComposeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 15.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Write Button",
            tint = MaterialTheme.colorScheme.surface
        )
    }
}
/* ____________________________________________________________________*/

@Composable
private fun MockView(state: PostListViewModel.ViewState) {
    val mockViewModel = remember { MockPostListViewModel(initialState = state) }
    PostListView(viewModel = mockViewModel, navController = rememberNavController())
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(PostListViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(PostListViewModel.ViewState.Loaded(AraPost.mockList())) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(PostListViewModel.ViewState.Error("Error Message")) }
}