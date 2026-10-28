package org.sparcs.soap.app.features.course.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseHistory
import org.sparcs.soap.app.domain.models.otl.CourseHistoryClass
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.domain.models.otl.sectionLabel
import org.sparcs.soap.app.features.lectureDetail.components.DetailSectionHeader
import org.sparcs.soap.app.shared.extensions.bleedHorizontally
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun CourseHistorySection(
    history: List<CourseHistory>,
    selectedProfessorID: Int?,
    onSelectProfessor: (Int?) -> Unit,
) {
    val listState = rememberLazyListState()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CourseHistoryHeader(history.size)

        LazyRow(
            state = listState,
            modifier = Modifier.bleedHorizontally(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
    DetailSectionHeader(stringResource(R.string.course_history)) {
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

    Card(
        modifier = Modifier
            .width(190.dp)
            .alpha(if (isMatched) 1f else 0.45f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "${entry.year} ${stringResource(entry.semester.rawValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (entry.myLectureID != null) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.course_taken),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
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

/** A section of one semester; tapping it filters the reviews to its professor. */
@Composable
private fun CourseHistoryClassRow(
    section: CourseHistoryClass,
    selectedProfessorID: Int?,
    isMySection: Boolean,
    onSelectProfessor: (Int?) -> Unit,
) {
    val isMatch = section.professors.any { it.id == selectedProfessorID }
    val label = section.sectionLabel
    val canSelect = section.professors.isNotEmpty()

    Surface(
        selected = isMatch,
        enabled = canSelect,
        onClick = { onSelectProfessor(if (isMatch) null else section.professors.first().id) },
        shape = MaterialTheme.shapes.small,
        color = if (isMatch) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (isMatch) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (section.section.isNotEmpty()) {
                Text(
                    text = section.section,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label.title ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                label.professors?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isMySection) {
                    Text(
                        text = stringResource(R.string.course_your_section),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun CourseHistory.containsProfessor(selectedProfessorID: Int?): Boolean {
    if (selectedProfessorID == null) return false
    return classes.any { section -> section.professors.any { it.id == selectedProfessorID } }
}

/** Filter chip that narrows the reviews to one professor, picked from a menu; sits in the reviews header. */
@Composable
fun CourseProfessorPicker(
    professors: List<Professor>,
    selectedProfessorID: Int?,
    onSelectProfessor: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = professors.find { it.id == selectedProfessorID }

    Box {
        // Elevated rather than outlined, so "All professors" doesn't sit in the header as an empty box.
        ElevatedFilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = {
                Text(
                    text = selected?.name ?: stringResource(R.string.course_all_professors),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            // Leaves room for the header's title and count beside it.
            modifier = Modifier.widthIn(max = 200.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProfessorMenuItem(
                name = stringResource(R.string.course_all_professors),
                selected = selected == null,
                onClick = {
                    onSelectProfessor(null)
                    expanded = false
                }
            )
            professors.forEach { professor ->
                ProfessorMenuItem(
                    name = professor.name,
                    selected = professor.id == selectedProfessorID,
                    onClick = {
                        onSelectProfessor(professor.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfessorMenuItem(name: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(name) },
        onClick = onClick,
        trailingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseHistoryClassRowPreview() {
    Theme {
        CourseHistoryClassRow(
            section = CourseHistoryClass(1, "<리더십 강좌>", "A", listOf(Professor(1, "Kim"))),
            selectedProfessorID = null,
            isMySection = true,
            onSelectProfessor = {}
        )
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
    Theme {
        DetailSectionHeader("Reviews", Modifier.padding(horizontal = 16.dp), count = 12) {
            CourseProfessorPicker(listOf(Professor(1, "Professor")), null, {})
        }
    }
}
