package org.sparcs.soap.app.features.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.search.components.CourseSection
import org.sparcs.soap.app.features.search.components.PostSection
import org.sparcs.soap.app.features.search.components.SearchFilterRow
import org.sparcs.soap.app.features.search.components.SearchNavigationBar
import org.sparcs.soap.app.features.search.components.TaxiSection
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewView
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModel
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.shared.viewModelMocks.MockSearchViewModel
import org.sparcs.soap.app.shared.views.contentViews.CategoryFilterContent
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.SearchCustomBar
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiPreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    viewModel: SearchViewModelProtocol = hiltViewModel<SearchViewModel>(),
    taxiPreviewViewModel: TaxiPreviewViewModelProtocol = hiltViewModel<TaxiPreviewViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState()
    val courseFilterState by viewModel.courseFilterState.collectAsState()
    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeFilterCategory by remember { mutableStateOf<CourseFilterCategory?>(null) }
    val filterSheetState = rememberModalBottomSheetState()

    val backStackEvent = {
        navController.navigate(Channel.Start.name) {
            popUpTo(0) { inclusive = true }
        }
    }

    BackHandler {
        backStackEvent()
    }

    LaunchedEffect(searchScope) {
        if (searchScope == SearchScope.All) {
            if (searchText.isNotEmpty()) {
                viewModel.fetchInitialData()
            }
        } else {
            viewModel.loadFull()
        }
    }
    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    Scaffold(
        topBar = {
            SearchNavigationBar(
                scrollState = scrollState,
                scrollBehavior = topBarScrollBehavior
            )
        },
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .navigationBarsPadding()
            .analyticsScreen("Search")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchControlCard(
                    searchText = searchText,
                    searchScope = searchScope,
                    courseFilterState = courseFilterState,
                    onSearchTextChange = viewModel::onSearchTextChange,
                    onScopeChange = viewModel::onScopeChange,
                    onCategoryClick = { activeFilterCategory = it },
                    onResetFilters = { viewModel.onFilterChange(CourseFilterState()) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    SearchResultContent(
                        state = state,
                        searchText = searchText,
                        courseFilterState = courseFilterState,
                        viewModel = viewModel,
                        navController = navController,
                        coroutineScope = coroutineScope,
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

        activeFilterCategory?.let { category ->
            ModalBottomSheet(
                onDismissRequest = { activeFilterCategory = null },
                sheetState = filterSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CategoryFilterContent(
                    category = category,
                    selectedFilters = courseFilterState,
                    onFilterChange = viewModel::onFilterChange,
                    options = CourseFilterProvider.getOptions(category)
                )
            }
        }
    }
}

@Composable
private fun SearchControlCard(
    searchText: String,
    searchScope: SearchScope,
    courseFilterState: CourseFilterState,
    onSearchTextChange: (String) -> Unit,
    onScopeChange: (SearchScope) -> Unit,
    onCategoryClick: (CourseFilterCategory) -> Unit,
    onResetFilters: () -> Unit
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassBorder(shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            SearchCustomBar(
                value = searchText,
                onValueChange = onSearchTextChange,
                onValueClear = { onSearchTextChange("") },
                placeHolder = stringResource(R.string.search)
            )

            SearchFilterRow(
                searchScope = searchScope,
                courseFilterState = courseFilterState,
                onScopeChange = onScopeChange,
                onCategoryClick = onCategoryClick,
                onResetFilters = onResetFilters
            )
        }
    }
}

@Composable
private fun SearchResultContent(
    state: SearchViewModel.ViewState,
    searchText: String,
    courseFilterState: CourseFilterState,
    viewModel: SearchViewModelProtocol,
    navController: NavController,
    coroutineScope: CoroutineScope,
    onTaxiClick: (TaxiRoom) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state is SearchViewModel.ViewState.Error -> {
                ErrorView(
                    error = (state).error,
                    onRetry = { coroutineScope.launch { viewModel.bind() } }
                )
            }

            searchText.isEmpty() && courseFilterState.isEmpty() -> {
                UnavailableView(
                    icon = Icons.Rounded.Search,
                    title = stringResource(R.string.search_anything),
                    description = stringResource(R.string.find_etc)
                )
            }

            else -> {
                ResultView(
                    viewModel = viewModel,
                    navController = navController,
                    onTaxiClick = onTaxiClick
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
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
    SearchView(
        viewModel = mockViewModel,
        taxiPreviewViewModel = PreviewTaxiPreviewViewModel(),
        navController = rememberNavController()
    )
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
@Preview(widthDp = 840, heightDp = 480)
private fun LoadedLandscapePreview() {
    Theme { MockView(SearchViewModel.ViewState.Loaded) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(SearchViewModel.ViewState.Error(Exception())) }
}