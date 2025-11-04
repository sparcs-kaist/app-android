package com.example.soap.Features.Search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.SearchScope
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Search.Components.CourseSection
import com.example.soap.Features.Search.Components.PostSection
import com.example.soap.Features.Search.Components.SearchNavigationBar
import com.example.soap.Features.Search.Components.TaxiSection
import com.example.soap.R
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.Shared.Views.ContentViews.SearchCourses
import com.example.soap.Shared.Views.ContentViews.UnavailableView
import com.example.soap.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun SearchView(
    viewModel: SearchViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState()
    val scrollState = rememberScrollState()

    val coroutine = rememberCoroutineScope()
    var isTaxiSheetOpen by remember { mutableStateOf(false) }
    var selectedTaxiId by remember { mutableStateOf<Int?>(null) }

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
            SearchCourses(
                value = searchText,
                onValueChange = { value ->
                    viewModel.onSearchTextChange(value)
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
                        label = { Text(scope.description) }
                    )
                }
            }

            when {
                state is SearchViewModel.ViewState.Error -> {
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = (state as SearchViewModel.ViewState.Error).message,
                        onRetry = { coroutine.launch { viewModel.bind() } }
                    )
                }

                searchText.isEmpty() -> {
                    UnavailableView(
                        icon = painterResource(R.drawable.search),
                        title = "Search Anything",
                        description = "Find courses, posts, rides and more."
                    )
                }

                else -> {
                    ResultView(
                        viewModel = viewModel,
                        navController = navController,
                        onTaxiClick = {}
                        //TODO navcontroller
                    )
                }
            }
        }
        //TaxiBottomSheet
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
                    courses = if (state == SearchViewModel.ViewState.Loading)
                        courses.take(3)
                    else courses.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    navController = navController
                )
            }
        } else if (searchScope == SearchScope.Courses) {
            item {
                CourseSection(
                    courses = if (state == SearchViewModel.ViewState.Loading)
                        courses
                    else courses,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    navController = navController
                )
            }
        }

        // --- Posts ---
        if (searchScope == SearchScope.All) {
            item {
                PostSection(
                    posts = if (state == SearchViewModel.ViewState.Loading)
                        posts.take(3)
                    else posts.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onLoadMore = { viewModel.loadAraNextPage() },
                    navController = navController
                )
            }
        } else if (searchScope == SearchScope.Posts) {
            item {
                PostSection(
                    posts = if (state == SearchViewModel.ViewState.Loading)
                        posts
                    else posts,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onLoadMore = { viewModel.loadAraNextPage() },
                    navController = navController
                )
            }
        }

        // --- Taxi Rooms ---
        if (searchScope == SearchScope.All) {
            item {
                TaxiSection(
                    rooms = if (state == SearchViewModel.ViewState.Loading)
                        rooms.take(3)
                    else rooms.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onTaxiClick = onTaxiClick
                )
            }
        } else if (searchScope == SearchScope.Rides) {
            item {
                TaxiSection(
                    rooms = if (state == SearchViewModel.ViewState.Loading)
                        rooms
                    else rooms,
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onTaxiClick = onTaxiClick,
                    onLoadMore = { viewModel.scopedFetch() }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { SearchView(MockSearchViewModel(), rememberNavController()) }
}
