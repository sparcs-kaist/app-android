package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.features.timetable.components.LectureListRow
import org.sparcs.soap.app.features.timetable.components.LectureListRowDetail
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun GradeEntryView(
    item: OTLUserLectureSemester,
    state: CreditCalculationViewState,
    onBack: () -> Unit,
    onGrade: (LectureGrade?, Int) -> Unit,
) {
    val timetable = state.timetables[item.id]
    CreditScreen(
        semesterTitle(item),
        onBack,
        subtitle = stringResource(R.string.credit_gpa, formatGPA(state.summary(item)?.gpa))
    ) { padding ->
        LazyColumn(
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
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = LocalTimetableTheme.current.backgroundColor
                        ?: MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBorder(RoundedCornerShape(28.dp))
                ) {
                    Box(
                        Modifier
                            .height(500.dp)
                            .padding(8.dp)
                    ) { TimetableGrid(timetable = timetable) }
                }
            }
            item {
                CreditCard {
                    val lectures = timetable?.lectures.orEmpty()
                    Text(
                        pluralStringResource(
                            R.plurals.lectures_count,
                            lectures.size,
                            lectures.size
                        ),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                    )
                    lectures.forEachIndexed { index, lecture ->
                        GradeEntryRow(
                            lecture,
                            state.grades[lecture.id],
                            lecture.id in state.supersededLectureIDs
                        ) { onGrade(it, lecture.id) }
                        if (index < lectures.lastIndex) HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeEntryRow(
    lecture: Lecture,
    grade: LectureGrade?,
    isSuperseded: Boolean = false,
    onGrade: (LectureGrade?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.credit_grade_for, lecture.name)
    Box {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LectureListRow(
                lecture = lecture,
                modifier = Modifier.weight(1f),
                detail = LectureListRowDetail.GRADING,
                badge = if (isSuperseded) stringResource(R.string.credit_retaken) else null,
            )
            Box {
                val tint =
                    if (grade == null) Color(0xFFEF8B23) else MaterialTheme.colorScheme.primary
                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .semantics { contentDescription = description },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = tint.copy(alpha = 0.15f),
                        contentColor = tint
                    )
                ) {
                    Text(grade?.title ?: "\u2014", fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (grade != null) DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.credit_clear_grade),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = { onGrade(null); expanded = false }
                    )
                    LectureGrade.options(lecture).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(gradeTitle(option)) },
                            trailingIcon = {
                                if (grade == option) Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { onGrade(option); expanded = false })
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
    Theme {
        GradeEntryRow(
            creditPreviewState().timetables.values.first().lectures.first(),
            LectureGrade.A_PLUS,
            onGrade = {})
    }
}
