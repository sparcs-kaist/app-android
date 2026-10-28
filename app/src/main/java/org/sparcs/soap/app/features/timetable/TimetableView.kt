package org.sparcs.soap.app.features.timetable

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.rememberLectureSearchStyle
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchStyle
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.timetable.components.ActivityList
import org.sparcs.soap.app.features.timetable.components.CompactTimetableSelector
import org.sparcs.soap.app.features.timetable.components.LectureList
import org.sparcs.soap.app.features.timetable.components.TimetableAddButton
import org.sparcs.soap.app.features.timetable.components.TimetableCreditGraph
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetableOfflineStatus
import org.sparcs.soap.app.features.timetable.components.TimetableOnboardingTips
import org.sparcs.soap.app.features.timetable.components.TimetableSummary
import org.sparcs.soap.app.features.timetable.components.TimetableViewNavigationBar
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewModelProtocol
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditsSummaryCard
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareSheet
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareSnapshot
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.escapeHash
import org.sparcs.soap.app.shared.sharing.ScreenshotEffect
import org.sparcs.soap.app.shared.sharing.navigateToShareFeed
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TimetableView(
    viewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    navController: NavController,
    creditViewModel: CreditCalculationViewModelProtocol? = null,
) {
    val creditState = creditViewModel?.state?.collectAsState()?.value
    TimetableLifecycleEffect(viewModel)
    val loadState by viewModel.loadState.collectAsState()
    val creditsContent: @Composable () -> Unit = {
        creditState?.let { CreditsSummaryCard(it) { navController.navigate(Channel.CreditCalculation.name) } }
    }
    val scrollState = rememberScrollState()
    var lectureToDelete by remember { mutableStateOf<Lecture?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp

    var shareSnapshot by remember { mutableStateOf<TimetableShareSnapshot?>(null) }
    var isSelectorDialogPresented by remember { mutableStateOf(false) }
    val timetableTheme = LocalTimetableTheme.current
    val onShareClick: () -> Unit = {
        shareSnapshot = TimetableShareSnapshot.of(
            viewModel.selectedSemester.value,
            viewModel.selectedTimetable.value,
            timetableTheme
        )
    }
    shareSnapshot?.let { snapshot ->
        TimetableShareSheet(
            snapshot,
            onDismiss = { shareSnapshot = null },
            onFeed = navController::navigateToShareFeed
        )
    }

    val selectedTimetable by viewModel.selectedTimetable.collectAsState()
    val isEditable by viewModel.isEditable.collectAsState()
    val timetableName by viewModel.timetableName.collectAsState()
    val lectureSearchStyle by rememberLectureSearchStyle()

    val candidateLecture by viewModel.candidateLecture.collectAsState()
    val isOverlapping by viewModel.isCandidateOverlapping.collectAsState()

    val tipStore = LocalTipStore.current
    val openLectureSearch: () -> Unit = {
        tipStore?.invalidate(BuddyTip.AddEvent)
        navController.navigate(
            if (lectureSearchStyle == LectureSearchStyle.Fixed) Channel.CourseCompose.name
            else Channel.LectureSearch.name
        )
    }
    val openActivityCreation: () -> Unit = {
        selectedTimetable?.id?.let {
            tipStore?.invalidate(BuddyTip.AddEvent)
            navController.navigate("${Channel.ActivityCreation.name}/$it")
        }
    }

    BackHandler(enabled = candidateLecture != null) {
        viewModel.setCandidateLecture(null)
    }

    ScreenshotEffect {
        if (shareSnapshot == null && !showDeleteDialog && candidateLecture == null &&
            !viewModel.isAlertPresented && !isSelectorDialogPresented
        ) onShareClick()
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!isLandscape) {
                    TimetableViewNavigationBar(
                        isButtonEnabled = isEditable,
                        onClick = openLectureSearch,
                        onActivityClick = openActivityCreation,
                        scrollBehavior = topBarScrollBehavior
                    )
                }
            },
            modifier = Modifier
                .hideTopBarOnScroll(topBarScrollBehavior.takeUnless { isLandscape })
                .analyticsScreen("Timetable")
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = loadState.isRefreshing,
                onRefresh = {
                    viewModel.fetchData()
                    creditViewModel?.refresh()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLandscape) {
                    TimetableLandscapeLayout(
                        viewModel = viewModel,
                        timetableName = timetableName,
                        onShareClick = onShareClick,
                        selectedTimetable = selectedTimetable,
                        navController = navController,
                        onDeleteClick = { lecture ->
                            lectureToDelete = lecture
                            showDeleteDialog = true
                        },
                        onAddClick = openLectureSearch,
                        onActivityClick = openActivityCreation,
                        isEditable = isEditable,
                        creditsContent = creditsContent,
                        onSelectorDialogChange = { isSelectorDialogPresented = it },
                    )
                } else {
                    TimetablePortraitLayout(
                        viewModel = viewModel,
                        timetableName = timetableName,
                        onShareClick = onShareClick,
                        selectedTimetable = selectedTimetable,
                        screenHeight = screenHeight,
                        navController = navController,
                        onDeleteClick = { lecture ->
                            lectureToDelete = lecture
                            showDeleteDialog = true
                        },
                        scrollState = scrollState,
                        creditsContent = creditsContent,
                        onSelectorDialogChange = { isSelectorDialogPresented = it },
                    )
                }
            }
        }

        if (showDeleteDialog && lectureToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteLecture(lecture = lectureToDelete!!)
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = { Text(stringResource(R.string.delete)) },
                text = {
                    Text(
                        stringResource(
                            R.string.do_you_really_want_to_delete_this_table,
                            lectureToDelete!!.name
                        )
                    )
                }
            )
        }


    }
}

@Composable
private fun TimetableLandscapeLayout(
    viewModel: TimetableViewModelProtocol,
    timetableName: String,
    onShareClick: () -> Unit,
    selectedTimetable: Timetable?,
    navController: NavController,
    onDeleteClick: (Lecture) -> Unit,
    onAddClick: () -> Unit,
    onActivityClick: () -> Unit,
    isEditable: Boolean,
    creditsContent: @Composable () -> Unit,
    onSelectorDialogChange: (Boolean) -> Unit,
) {
    val loadState by viewModel.loadState.collectAsState()
    val selectedTimetableID by viewModel.selectedTimetableID.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.timetable),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                CompactTimetableSelector(
                    viewModel,
                    timetableName,
                    onShareClick = onShareClick,
                    onDialogVisibilityChange = onSelectorDialogChange,
                )
                Spacer(modifier = Modifier.width(12.dp))
                TimetableAddButton(
                    enabled = isEditable, onAddClass = onAddClick, onAddActivity = onActivityClick
                )
            }
        }

        if (loadState.isOffline) {
            TimetableOfflineStatus(loadState, Modifier.padding(horizontal = 24.dp))
        }

        TimetableOnboardingTips(
            isMyTable = selectedTimetableID == null,
            isEditable = isEditable,
            onNewTable = viewModel::createTable,
            onDuplicateMyTable = viewModel::duplicateMyTable,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val availableHeight = maxHeight
            val density = LocalDensity.current
            var rightColumnHeight by remember { mutableStateOf(0.dp) }
            val contentHeight = maxOf(availableHeight, rightColumnHeight)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(contentHeight),
                        colors = CardDefaults.cardColors(
                            containerColor = LocalTimetableTheme.current.backgroundColor
                                ?: MaterialTheme.colorScheme.background
                        ),
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            TimetableGrid(
                                viewModel = viewModel,
                                onLectureSelected = { lecture ->
                                    val json = Gson().toJson(lecture).escapeHash()
                                    navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                },
                                showDeleteDialog = onDeleteClick,
                                onEditActivity = { navController.navigate("${Channel.ActivityCreation.name}/${selectedTimetable?.id}?activityId=${it.id}") }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .onSizeChanged {
                                rightColumnHeight = with(density) { it.height.toDp() }
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                LectureList(
                                    lectures = selectedTimetable?.lectures ?: emptyList(),
                                    onLectureSelected = { lecture ->
                                        val json = Gson().toJson(lecture).escapeHash()
                                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                    }
                                )
                            }
                        }

                        selectedTimetable?.takeIf { it.id.toIntOrNull() != null }?.let { table ->
                            ActivityList(
                                activities = table.activities, viewModel = viewModel,
                                onEdit = { navController.navigate("${Channel.ActivityCreation.name}/${table.id}?activityId=${it.id}") }
                            )
                        }

                        selectedTimetable?.let { TimetableCreditGraph(it) }

                        TimetableSummary(viewModel)
                        creditsContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetablePortraitLayout(
    viewModel: TimetableViewModelProtocol,
    timetableName: String,
    onShareClick: () -> Unit,
    selectedTimetable: Timetable?,
    screenHeight: Dp,
    navController: NavController,
    onDeleteClick: (Lecture) -> Unit,
    scrollState: ScrollState,
    creditsContent: @Composable () -> Unit,
    onSelectorDialogChange: (Boolean) -> Unit,
) {
    val loadState by viewModel.loadState.collectAsState()
    val selectedTimetableID by viewModel.selectedTimetableID.collectAsState()
    val isEditable by viewModel.isEditable.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompactTimetableSelector(
            viewModel,
            timetableName,
            modifier = Modifier.fillMaxWidth(),
            isWide = true,
            onShareClick = onShareClick,
            onDialogVisibilityChange = onSelectorDialogChange,
        )

        if (loadState.isOffline) {
            TimetableOfflineStatus(loadState)
        }

        TimetableOnboardingTips(
            isMyTable = selectedTimetableID == null,
            isEditable = isEditable,
            onNewTable = viewModel::createTable,
            onDuplicateMyTable = viewModel::duplicateMyTable,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.66f),
            colors = CardDefaults.cardColors(
                containerColor = LocalTimetableTheme.current.backgroundColor
                    ?: MaterialTheme.colorScheme.background
            ),
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                TimetableGrid(
                    viewModel = viewModel,
                    onLectureSelected = { lecture ->
                        val json = Gson().toJson(lecture).escapeHash()
                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                    },
                    showDeleteDialog = onDeleteClick,
                    onEditActivity = { navController.navigate("${Channel.ActivityCreation.name}/${selectedTimetable?.id}?activityId=${it.id}") }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                LectureList(
                    lectures = selectedTimetable?.lectures ?: emptyList(),
                    onLectureSelected = { lecture ->
                        val json = Gson().toJson(lecture).escapeHash()
                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                    }
                )
            }
        }

        selectedTimetable?.takeIf { it.id.toIntOrNull() != null }?.let { table ->
            ActivityList(
                activities = table.activities, viewModel = viewModel,
                onEdit = { navController.navigate("${Channel.ActivityCreation.name}/${table.id}?activityId=${it.id}") }
            )
        }

        selectedTimetable?.let { TimetableCreditGraph(it) }

        TimetableSummary(viewModel)
        creditsContent()
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        TimetableView(
            navController = rememberNavController(),
            viewModel = PreviewTimetableViewModel()
        )
    }
}
