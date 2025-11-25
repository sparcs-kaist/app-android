package com.sparcs.soap.Features.Search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparcs.soap.Domain.Models.SearchScope
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Features.NavigationBar.AppDownBar
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.Search.Components.CourseSection
import com.sparcs.soap.Features.Search.Components.PostSection
import com.sparcs.soap.Features.Search.Components.SearchNavigationBar
import com.sparcs.soap.Features.Search.Components.TaxiSection
import com.sparcs.soap.Features.TaxiPreview.TaxiPreviewView
import com.sparcs.soap.Features.TaxiPreview.TaxiPreviewViewModelProtocol
import com.sparcs.soap.R
import com.sparcs.soap.Shared.ViewModelMocks.MockSearchViewModel
import com.sparcs.soap.Shared.Views.ContentViews.ErrorView
import com.sparcs.soap.Shared.Views.ContentViews.SearchCustomBar
import com.sparcs.soap.Shared.Views.ContentViews.UnavailableView
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    viewModel: SearchViewModelProtocol = hiltViewModel(),
    taxiPreviewViewModel: TaxiPreviewViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState()
    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(searchScope) {
        if (searchScope == SearchScope.All) {
            if (searchText.isNotEmpty()) {
                viewModel.fetchInitialData()
            }
        } else {
            viewModel.loadFull()
        }
    }
    Scaffold(
        topBar = {
            SearchNavigationBar(scrollState = scrollState)
        },
        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.SearchView
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(innerPadding)
        ) {
            SearchCustomBar(
                value = searchText,
                onValueChange = { value ->
                    viewModel.onSearchTextChange(value)
                },
                onValueClear = {
                    viewModel.onSearchTextChange("")
                },
                placeHolder = stringResource(R.string.search)
            )

            Row(
                Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SearchScope.entries.forEach { scope ->
                    FilterChip(
                        selected = (searchScope == scope),
                        onClick = { viewModel.onScopeChange(scope) },
                        label = { Text(stringResource(scope.labelRes)) }
                    )
                }
            }

            when {
                state is SearchViewModel.ViewState.Error -> {
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = (state as SearchViewModel.ViewState.Error).message,
                        onRetry = { coroutineScope.launch { viewModel.bind() } }
                    )
                }

                searchText.isEmpty() -> {
                    UnavailableView(
                        icon = painterResource(R.drawable.search),
                        title = stringResource(R.string.search_anything),
                        description = stringResource(R.string.find_etc)
                    )
                }

                else -> {
                    ResultView(
                        viewModel = viewModel,
                        navController = navController,
                        onTaxiClick = { selectedRoom = it }
                    )
                }
            }
        }

        selectedRoom?.let { room ->
            ModalBottomSheet(
                onDismissRequest = {
                    selectedRoom = null
                },
                sheetState = sheetState,
                dragHandle = {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .padding(top = 4.dp)
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.grayBB)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                TaxiPreviewView(
                    room = room,
                    viewModel = taxiPreviewViewModel,
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            selectedRoom = null
                        }
                    },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ResultView(
    viewModel: SearchViewModelProtocol,
    navController: NavController,
    onTaxiClick: (TaxiRoom) -> Unit,
) {
    val courses by viewModel.courses.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val rooms by viewModel.taxiRooms.collectAsState()

    val searchScope by viewModel.searchScope.collectAsState()
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // --- Courses ---
        if (searchScope == SearchScope.All) {
            item {
                CourseSection(
                    courses = courses.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    navController = navController,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        } else if (searchScope == SearchScope.Courses) {
            item {
                CourseSection(
                    courses = courses,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    navController = navController,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        }

        // --- Posts ---
        if (searchScope == SearchScope.All) {
            item {
                PostSection(
                    posts = posts.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onLoadMore = { viewModel.loadAraNextPage() },
                    navController = navController,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        } else if (searchScope == SearchScope.Posts) {
            item {
                PostSection(
                    posts = posts,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onLoadMore = { viewModel.loadAraNextPage() },
                    navController = navController,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        }

        // --- Taxi Rooms ---
        if (searchScope == SearchScope.All) {
            item {
                TaxiSection(
                    rooms = rooms.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onTaxiClick = onTaxiClick,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        } else if (searchScope == SearchScope.Rides) {
            item {
                TaxiSection(
                    rooms = rooms,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onTaxiClick = onTaxiClick,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        }
    }
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: SearchViewModel.ViewState) {
    val mockViewModel = remember { MockSearchViewModel(initialState = state) }
    SearchView(viewModel = mockViewModel, navController = rememberNavController())
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(SearchViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(SearchViewModel.ViewState.Loaded) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(SearchViewModel.ViewState.Error("Error Message")) }
}