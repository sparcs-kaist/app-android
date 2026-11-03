package org.sparcs.soap.app.features.search

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.search.components.CoursePaginationFooter
import org.sparcs.soap.app.features.search.components.CourseSection
import org.sparcs.soap.app.features.search.components.PostSection
import org.sparcs.soap.app.features.search.components.SearchFilterRow
import org.sparcs.soap.app.features.search.components.TaxiSection
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewView
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModel
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.onlyBottom
import org.sparcs.soap.app.shared.extensions.plus
import org.sparcs.soap.app.shared.extensions.withoutBottom
import org.sparcs.soap.app.shared.viewModelMocks.MockSearchViewModel
import org.sparcs.soap.app.shared.views.contentViews.CategoryFilterContent
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.MaterialSearchField
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme
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

    val coroutineScope = rememberCoroutineScope()
    var selectedRoom by remember { mutableStateOf<TaxiRoom?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeFilterCategory by remember { mutableStateOf<CourseFilterCategory?>(null) }
    val filterSheetState = rememberModalBottomSheetState()

    // The search bar and scope chips slide away together while results scroll down and come back
    // as soon as they scroll up, leaving more of a phone screen to the results.
    val topBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val showsResults = state !is SearchViewModel.ViewState.Error &&
        !(searchText.isBlank() && (searchScope != SearchScope.Courses || courseFilterState.isEmpty()))
    // A new scope or an empty or error state starts a fresh page, so bring the bar back: those
    // states don't scroll, and a hidden bar would have no way to return.
    LaunchedEffect(searchScope, showsResults) {
        val scrollState = topBarScrollBehavior.scrollState
        animate(scrollState.scrollOffset, 0f) { value, _ -> scrollState.scrollOffset = value }
    }

    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .then(topBarScrollBehavior.searchBarScrollBehaviorModifier)
                    .padding(top = 8.dp)
            ) {
                Column(Modifier.widthIn(max = 600.dp)) {
                    MaterialSearchField(
                        query = searchText,
                        onQueryChange = viewModel::onSearchTextChange,
                        placeholder = stringResource(R.string.search),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SearchFilterRow(
                        searchScope = searchScope,
                        courseFilterState = courseFilterState,
                        onScopeChange = viewModel::onScopeChange,
                        onCategoryClick = { activeFilterCategory = it },
                        onResetFilters = { viewModel.onFilterChange(CourseFilterState()) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .navigationBarsPadding()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .analyticsScreen("Search")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding.withoutBottom()),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                SearchResultContent(
                    state = state,
                    searchText = searchText,
                    courseFilterState = courseFilterState,
                    viewModel = viewModel,
                    navController = navController,
                    coroutineScope = coroutineScope,
                    bottomPadding = innerPadding.onlyBottom(),
                    onTaxiClick = { selectedRoom = it }
                )
            }
        }

        selectedRoom?.let { room ->
            ModalBottomSheet(
                onDismissRequest = {
                    selectedRoom = null
                },
                sheetState = sheetState,
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
private fun SearchResultContent(
    state: SearchViewModel.ViewState,
    searchText: String,
    courseFilterState: CourseFilterState,
    viewModel: SearchViewModelProtocol,
    navController: NavController,
    coroutineScope: CoroutineScope,
    bottomPadding: PaddingValues,
    onTaxiClick: (TaxiRoom) -> Unit
) {
    val searchScope by viewModel.searchScope.collectAsState()
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

            searchText.isBlank() && (searchScope != SearchScope.Courses || courseFilterState.isEmpty()) -> {
                val isCourses = searchScope == SearchScope.Courses
                UnavailableView(
                    icon = Icons.Rounded.Search,
                    title = stringResource(if (isCourses) R.string.search_courses else R.string.search_anything),
                    description = stringResource(if (isCourses) R.string.search_courses_description else R.string.find_etc)
                )
            }

            else -> {
                ResultView(
                    viewModel = viewModel,
                    navController = navController,
                    onTaxiClick = onTaxiClick,
                    bottomPadding = bottomPadding,
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
    bottomPadding: PaddingValues = PaddingValues(),
) {
    val hasMoreCourses by viewModel.hasMoreCourses.collectAsState()
    val isLoadingMoreCourses by viewModel.isLoadingMoreCourses.collectAsState()
    val coursePageError by viewModel.coursePageError.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val rooms by viewModel.taxiRooms.collectAsState()

    val searchScope by viewModel.searchScope.collectAsState()
    val state by viewModel.state.collectAsState()

    val openPost: (AraPost) -> Unit = { post ->
        viewModel.markRead(post.id)
        navController.navigate(Channel.PostView.name + "?postId=${post.id}")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp) + bottomPadding
    ) {
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
            item {
                CoursePaginationFooter(hasMoreCourses, isLoadingMoreCourses, coursePageError, courses.size, viewModel::loadCoursesNextPage)
            }
        }

        if (searchScope == SearchScope.All) {
            item {
                PostSection(
                    posts = posts.take(3),
                    searchScope = searchScope,
                    onScopeChange = viewModel::onScopeChange,
                    onLoadMore = { viewModel.loadAraNextPage() },
                    onPostClick = openPost,
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
                    onPostClick = openPost,
                    isSkeleton = state == SearchViewModel.ViewState.Loading
                )
            }
        }

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
