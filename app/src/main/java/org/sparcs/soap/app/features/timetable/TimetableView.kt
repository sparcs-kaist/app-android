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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.lectureSearch.LectureSearchView
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.AppDownBar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.navigationBar.components.AddButton
import org.sparcs.soap.app.features.timetable.components.CompactTimetableSelector
import org.sparcs.soap.app.features.timetable.components.LectureList
import org.sparcs.soap.app.features.timetable.components.TimetableBottomSheet
import org.sparcs.soap.app.features.timetable.components.TimetableCreditGraph
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetableSummary
import org.sparcs.soap.app.features.timetable.components.TimetableViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.escapeHash
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TimetableView(
    viewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
    navController: NavController,
) {
    val scrollState = rememberScrollState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var lectureToDelete by remember { mutableStateOf<Lecture?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp

    val selectedTimetable by viewModel.selectedTimetable.collectAsState()
    val isEditable by viewModel.isEditable.collectAsState()
    val timetableName by viewModel.timetableName.collectAsState()

    val backStackEvent = {
        navController.navigate(Channel.Start.name) {
            popUpTo(0) { inclusive = true }
        }
    }

    BackHandler {
        backStackEvent()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                if (!isLandscape) {
                    TimetableViewNavigationBar(
                        scrollState = scrollState,
                        isButtonEnabled = isEditable,
                        onClick = { expanded = true }
                    )
                }
            },
            bottomBar = {
                AppDownBar(
                    navController = navController,
                    currentScreen = Channel.TimeTable
                )
            },
            modifier = Modifier.analyticsScreen("Timetable")
        ) { innerPadding ->
            Box(
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
                        selectedTimetable = selectedTimetable,
                        navController = navController,
                        onDeleteClick = { lecture ->
                            lectureToDelete = lecture
                            showDeleteDialog = true
                        },
                        onAddClick = { expanded = true },
                        isEditable = isEditable
                    )
                } else {
                    TimetablePortraitLayout(
                        viewModel = viewModel,
                        timetableName = timetableName,
                        selectedTimetable = selectedTimetable,
                        screenHeight = screenHeight,
                        navController = navController,
                        onDeleteClick = { lecture ->
                            lectureToDelete = lecture
                            showDeleteDialog = true
                        },
                        scrollState = scrollState
                    )
                }
            }
        }

        if (expanded) {
            TimetableBottomSheet(
                onDismiss = {
                    expanded = false
                    lectureSearchViewModel.onSearchTextChange("")
                }
            ) { onFold ->
                LectureSearchView(
                    navController = navController,
                    timetableViewModel = viewModel,
                    lectureSearchViewModel = lectureSearchViewModel,
                    timetableName = timetableName,
                ) {
                    onFold()
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

        if (viewModel.showAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.showAlert = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.showAlert = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = { Text(stringResource(R.string.error)) },
                text = {
                    viewModel.alertMessageRes?.let { Text(stringResource(it)) }
                }
            )
        }
    }
}

@Composable
private fun TimetableLandscapeLayout(
    viewModel: TimetableViewModelProtocol,
    timetableName: String,
    selectedTimetable: Timetable?,
    navController: NavController,
    onDeleteClick: (Lecture) -> Unit,
    onAddClick: () -> Unit,
    isEditable: Boolean
) {
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
                CompactTimetableSelector(viewModel, timetableName)
                Spacer(modifier = Modifier.width(12.dp))
                AddButton(
                    contentDescription = "Add Timetable",
                    onClick = onAddClick,
                    isEnabled = isEditable
                )
            }
        }

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
                            .height(contentHeight)
                            .glassBorder(shape = RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            TimetableGrid(
                                viewModel = viewModel,
                                onLectureSelected = { lecture ->
                                    val json = Gson().toJson(lecture).escapeHash()
                                    navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                },
                                showDeleteDialog = onDeleteClick
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
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassBorder(shape = RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                                LectureList(
                                    lectures = selectedTimetable?.lectures ?: emptyList(),
                                    onLectureSelected = { lecture ->
                                        val json = Gson().toJson(lecture).escapeHash()
                                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                    }
                                )
                            }
                        }

                        selectedTimetable?.let { TimetableCreditGraph(it) }

                        TimetableSummary(viewModel)
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
    selectedTimetable: Timetable?,
    screenHeight: Dp,
    navController: NavController,
    onDeleteClick: (Lecture) -> Unit,
    scrollState: ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompactTimetableSelector(viewModel, timetableName, modifier = Modifier.fillMaxWidth(), isWide = true)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(28.dp))
                .height(screenHeight * 0.66f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                TimetableGrid(
                    viewModel = viewModel,
                    onLectureSelected = { lecture ->
                        val json = Gson().toJson(lecture).escapeHash()
                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                    },
                    showDeleteDialog = onDeleteClick
                )
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                LectureList(
                    lectures = selectedTimetable?.lectures ?: emptyList(),
                    onLectureSelected = { lecture ->
                        val json = Gson().toJson(lecture).escapeHash()
                        navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                    }
                )
            }
        }

        selectedTimetable?.let { TimetableCreditGraph(it) }

        TimetableSummary(viewModel)
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        TimetableView(
            navController = rememberNavController(),
            lectureSearchViewModel = PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded),
            viewModel = PreviewTimetableViewModel()
        )
    }
}
