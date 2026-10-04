package org.sparcs.soap.app.features.lectureSearch.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableRangeSelection
import org.sparcs.soap.app.features.courseCompose.components.CourseComposeTopBar
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableRangeSelector
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureTimeRangePage(
    viewModel: LectureSearchViewModelProtocol,
    timetableViewModel: TimetableViewModelProtocol,
    navController: NavController,
) {
    val timetable by timetableViewModel.selectedTimetable.collectAsState()
    val time by viewModel.time.collectAsState()
    LectureTimeRangeContent(
        timetable,
        time,
        viewModel::onTimeChange
    ) { navController.popBackStack() }
}

@Composable
fun LectureTimeRangeContent(
    timetable: Timetable?,
    time: LectureTimeFilter,
    onTimeChange: (LectureTimeFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val begin = time.begin ?: TimetableRangeSelection.start
    val end = time.end ?: TimetableRangeSelection.end
    Scaffold(
        modifier = Modifier.analyticsScreen("Lecture Time Range"),
        topBar = {
            CourseComposeTopBar(
                title = stringResource(R.string.lecture_time_filter),
                onClose = onDismiss,
                actions = {
                    TextButton(
                        onClick = { onTimeChange(LectureTimeFilter()) },
                        enabled = !time.isEmpty
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
                },
            )
        },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .background(MaterialTheme.colorScheme.surface)) {
            Text(
                if (time.isEmpty) stringResource(R.string.time_range_hint)
                else listOfNotNull(
                    time.day?.let { stringResource(it.fullStringValue) },
                    "%02d:%02d - %02d:%02d".format(begin / 60, begin % 60, end / 60, end % 60),
                ).joinToString(" "),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(16.dp))
            TimetableRangeSelector(
                timetable, time, onTimeChange,
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .glassBorder(shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun LectureTimeRangePreview() {
    Theme {
        LectureTimeRangeContent(null, LectureTimeFilter(), {}, {})
    }
}
