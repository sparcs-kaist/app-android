package org.sparcs.soap.app.features.course.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseHistory
import org.sparcs.soap.app.domain.models.otl.CourseHistoryClass
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun CourseHistorySection(
    history: List<CourseHistory>,
    selectedProfessorID: Int?,
    onSelectProfessor: (Int?) -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedHistoryIndex = if (selectedProfessorID == null) {
        -1
    } else {
        history.indexOfFirst { it.containsProfessor(selectedProfessorID) }
    }

    LaunchedEffect(selectedProfessorID, history) {
        if (selectedHistoryIndex >= 0) {
            listState.animateScrollToItem(selectedHistoryIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CourseHistoryHeader(history.size)

        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history, key = { "${it.year}-${it.semester.intValue}" }) { entry ->
                CourseHistoryCard(
                    entry = entry,
                    selectedProfessorID = selectedProfessorID,
                    onSelectProfessor = onSelectProfessor
                )
            }
        }
    }
}

@Composable
private fun CourseHistoryHeader(historySize: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.course_history),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.course_offered_count, historySize),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CourseHistoryCard(
    entry: CourseHistory,
    selectedProfessorID: Int?,
    onSelectProfessor: (Int?) -> Unit,
) {
    val isMatched = selectedProfessorID == null || entry.containsProfessor(selectedProfessorID)

    Surface(
        modifier = Modifier
            .width(190.dp)
            .alpha(if (isMatched) 1f else 0.45f),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.year} ${stringResource(entry.semester.rawValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                if (entry.myLectureID != null) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            entry.classes.forEach { section ->
                CourseHistoryClassRow(
                    section = section,
                    selectedProfessorID = selectedProfessorID,
                    isMySection = section.lectureID == entry.myLectureID,
                    onSelectProfessor = onSelectProfessor
                )
            }
        }
    }
}

@Composable
private fun CourseHistoryClassRow(
    section: CourseHistoryClass,
    selectedProfessorID: Int?,
    isMySection: Boolean,
    onSelectProfessor: (Int?) -> Unit,
) {
    val isMatch = section.professors.any { it.id == selectedProfessorID }
    val professorText = section.professors.joinToString(", ") { it.name }.ifEmpty {
        stringResource(R.string.unknown)
    }
    val canSelect = section.professors.isNotEmpty()

    Surface(
        enabled = canSelect,
        onClick = { onSelectProfessor(if (isMatch) null else section.professors.first().id) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (section.section.isNotEmpty()) {
                    Text(
                        text = section.section,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = professorText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isMySection) {
                Text(
                    text = stringResource(R.string.course_your_section),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun CourseHistory.containsProfessor(selectedProfessorID: Int?): Boolean {
    if (selectedProfessorID == null) return false
    return classes.any { section -> section.professors.any { it.id == selectedProfessorID } }
}

@Composable
fun CourseProfessorPicker(
    professors: List<Professor>,
    selectedProfessorID: Int?,
    onSelectProfessor: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            onClick = { expanded = true },
            modifier = Modifier.align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = professors.find { it.id == selectedProfessorID }?.name
                        ?: stringResource(R.string.course_all_professors),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.course_all_professors),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSelectProfessor(null)
                        expanded = false
                    }
                )
                professors.forEach { professor ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = professor.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onSelectProfessor(professor.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun CourseHistorySectionPreview() {
    Theme { CourseHistorySection(Course.mock().history, null, {}) }
}

@Preview(showBackground = true)
@Composable
private fun CourseProfessorPickerPreview() {
    Theme { CourseProfessorPicker(listOf(Professor(1, "Professor")), null, {}) }
}
