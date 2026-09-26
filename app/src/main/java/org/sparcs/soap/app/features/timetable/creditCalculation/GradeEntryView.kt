package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.features.timetable.components.LectureListRow
import org.sparcs.soap.app.features.timetable.components.LectureListRowDetail
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun GradeEntryView(
    item: OTLUserLectureSemester,
    state: CreditCalculationViewState,
    onBack: () -> Unit,
    onGrade: (LectureGrade?, Int) -> Unit,
) {
    val timetable = state.timetables[item.id]
    CreditScreen(semesterTitle(item), onBack) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { state.summary(item)?.let { CreditCard { GradeSummary(it) } } }
            item {
                CreditCard { Box(Modifier.height(420.dp)) { TimetableGrid(timetable = timetable) } }
            }
            items(timetable?.lectures.orEmpty(), key = { it.id }) { lecture ->
                GradeEntryRow(lecture, state.grades[lecture.id], lecture.id in state.supersededLectureIDs) { onGrade(it, lecture.id) }
            }
        }
    }
}

@Composable
private fun GradeEntryRow(lecture: Lecture, grade: LectureGrade?, isSuperseded: Boolean = false, onGrade: (LectureGrade?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.credit_grade_for, lecture.name)
    CreditCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LectureListRow(
                lecture = lecture,
                modifier = Modifier.weight(1f),
                detail = LectureListRowDetail.GRADING,
                badge = if (isSuperseded) stringResource(R.string.credit_retaken) else null,
            )
            Box {
                TextButton(onClick = { expanded = true }, modifier = Modifier.semantics { contentDescription = description }) {
                    Text(grade?.title ?: stringResource(R.string.credit_enter_grade))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (grade != null) DropdownMenuItem(
                        text = { Text(stringResource(R.string.credit_clear_grade)) },
                        onClick = { onGrade(null); expanded = false }
                    )
                    LectureGrade.options(lecture).forEach { option ->
                        DropdownMenuItem(text = { Text(gradeTitle(option)) }, onClick = { onGrade(option); expanded = false })
                    }
                }
            }
        }
    }
}

@Composable
private fun gradeTitle(grade: LectureGrade): String = when (grade) {
    LectureGrade.PASS -> stringResource(R.string.credit_pass)
    LectureGrade.FAIL -> stringResource(R.string.credit_fail)
    LectureGrade.NON_RECORD -> stringResource(R.string.credit_non_record)
    LectureGrade.SATISFIED -> stringResource(R.string.credit_satisfied)
    LectureGrade.UNSATISFIED -> stringResource(R.string.credit_unsatisfied)
    else -> grade.title
}

@Preview
@Composable
private fun GradeEntryPreview() {
    val state = creditPreviewState()
    Theme { GradeEntryView(state.semesters.first(), state, {}, { _, _ -> }) }
}

@Preview
@Composable
private fun GradeEntryRowPreview() {
    Theme { GradeEntryRow(creditPreviewState().timetables.values.first().lectures.first(), LectureGrade.A_PLUS, onGrade = {}) }
}
