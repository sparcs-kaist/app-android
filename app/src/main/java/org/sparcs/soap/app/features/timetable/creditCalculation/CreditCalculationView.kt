package org.sparcs.soap.app.features.timetable.creditCalculation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.SemesterGradeSummary
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.navigationBar.components.DismissButton
import org.sparcs.soap.app.features.timetable.components.TimetableSilhouetteView
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun CreditCalculationView(
    navController: NavController,
    viewModel: CreditCalculationViewModel = hiltViewModel(),
) {
    val onBack: () -> Unit = { navController.popBackStack() }
    val state by viewModel.state.collectAsState()
    var selectedSemesterID by rememberSaveable { mutableStateOf<String?>(null) }
    var showsRequirements by rememberSaveable { mutableStateOf(false) }
    val selectedSemester = state.semesters.find { it.id == selectedSemesterID }
    BackHandler(selectedSemesterID != null || showsRequirements) {
        selectedSemesterID = null
        showsRequirements = false
    }
    when {
        state.isLoading || state.error != null -> CreditCalculationContent(
            state, onBack, { viewModel.load() }, {}, {}
        )

        showsRequirements -> CreditRequirementsView(
            state,
            { showsRequirements = false },
            viewModel::updateRequirements
        )

        selectedSemester != null -> GradeEntryView(
            selectedSemester, state, { selectedSemesterID = null }, viewModel::setGrade
        )

        else -> CreditCalculationContent(
            state, onBack, { viewModel.load() },
            { selectedSemesterID = it.id }, { showsRequirements = true })
    }
    if (state.saveError) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSaveError,
            containerColor = MaterialTheme.colorScheme.background,
            text = { Text(stringResource(R.string.credit_save_error)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissSaveError) {
                    Text(
                        stringResource(R.string.ok)
                    )
                }
            }
        )
    }
}

@Composable
internal fun CreditCalculationContent(
    state: CreditCalculationViewState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSemester: (OTLUserLectureSemester) -> Unit,
    onRequirements: () -> Unit,
) {
    CreditScreen(stringResource(R.string.credit_calculation), onBack) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ErrorView(
                    error = state.error,
                    defaultMessageResId = R.string.credit_load_error,
                    onRetry = onRetry
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.credit_summary_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                item { GPATrendChart(state) }
                item {
                    val description = stringResource(R.string.credit_requirements)
                    CreditCard(modifier = Modifier
                        .semantics { contentDescription = description }
                        .clickable(onClick = onRequirements)) {
                        GPASummaryContent(
                            state.overallSummary.gpa,
                            state.overallSummary.earnedCredits,
                            state.requirements.graduation
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.credit_semesters_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (state.semesters.isEmpty()) item { Text(stringResource(R.string.credit_empty)) }
                items(state.semesters.chunked(2), key = { it.first().id }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { semester ->
                            SemesterCard(
                                semester,
                                state,
                                Modifier.weight(1f)
                            ) { onSemester(semester) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                item {
                    Text(
                        stringResource(R.string.credit_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item { CreditsPrivacyFooter() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreditScreen(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    subtitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }, navigationIcon = { DismissButton(onBack) })
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            content(padding)
        }
    }
}

@Composable
internal fun CreditCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .glassBorder(RoundedCornerShape(20.dp))
    ) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SemesterCard(
    semester: OTLUserLectureSemester,
    state: CreditCalculationViewState,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val summary = state.summary(semester)
    val timetable = state.timetables[semester.id]
    CreditCard(
        modifier.clickable(enabled = timetable != null, onClick = onClick),
        PaddingValues(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                semesterTitle(semester), modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
            )
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TimetableSilhouetteView(timetable, Modifier
            .fillMaxWidth()
            .aspectRatio(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${formatGPA(summary?.gpa)} GPA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${summary?.recordedCredits ?: 0} ${stringResource(R.string.cr)}",
                    style = MaterialTheme.typography.labelMedium
                )
                if ((summary?.recordedAUs ?: 0) > 0) {
                    Text(
                        "${summary?.recordedAUs} ${stringResource(R.string.au)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        if (summary != null && !summary.isComplete) {
            Text(
                stringResource(
                    R.string.credit_completion,
                    summary.gradedCount,
                    summary.lectureCount
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SemesterCardPreview() {
    val state = creditPreviewState()
    Theme { SemesterCard(state.semesters.first(), state, Modifier, {}) }
}

@Composable
internal fun GradeSummary(summary: SemesterGradeSummary) {
    Text(
        stringResource(R.string.credit_gpa, formatGPA(summary.gpa)),
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        stringResource(R.string.credit_recorded, summary.recordedCredits),
        style = MaterialTheme.typography.bodyMedium
    )
    Text(
        stringResource(R.string.credit_completion, summary.gradedCount, summary.lectureCount),
        color = if (summary.isComplete) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
internal fun semesterTitle(semester: OTLUserLectureSemester): String =
    "${semester.year} ${stringResource(semester.semesterType.rawValue)}"

internal fun creditPreviewState(): CreditCalculationViewState {
    val table = Timetable.mock()
    val semester = OTLUserLectureSemester(2026, SemesterType.SPRING, emptyList())
    return CreditCalculationViewState(
        isLoading = false,
        semesters = listOf(semester),
        timetables = mapOf(semester.id to table),
        grades = table.lectures.associate { it.id to LectureGrade.A_MINUS },
        majorDepartments = table.lectures.take(1).map { it.department })
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreditCalculationPreview() {
    Theme { CreditCalculationContent(creditPreviewState(), {}, {}, {}, {}) }
}

@Preview
@Composable
private fun CreditSummaryPreview() {
    Theme { CreditCard { GradeSummary(creditPreviewState().overallSummary) } }
}
