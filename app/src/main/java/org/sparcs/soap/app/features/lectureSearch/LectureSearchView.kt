package org.sparcs.soap.app.features.lectureSearch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureSearch.components.CourseSearchSheetContent
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchViewNavigationBar
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableName: String,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
    onFoldSheet: () -> Unit = {},
    onExpandSheet: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            LectureSearchViewNavigationBar(
                title = stringResource(R.string.add_to_timetable, timetableName)
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Search")
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            CourseSearchSheetContent(
                navController = navController,
                timetableViewModel = timetableViewModel,
                lectureSearchViewModel = lectureSearchViewModel,
                onFoldSheet = onFoldSheet,
                onExpandSheet = onExpandSheet
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LectureRow(
    lecture: Lecture,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    val letters = listOf("?", "F", "F", "F", "D-", "D", "D+", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+")
    fun toLetter(value: Double) = letters.getOrElse(value.toInt().coerceIn(0, letters.lastIndex)) { "?" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onInfoClick
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isShortSection = lecture.section.isNotEmpty() && lecture.section.length <= 2
                    if (isShortSection) {
                        Text(
                            text = lecture.section,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = lecture.professors.firstOrNull()?.name ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val subtitleToDisplay = if (lecture.section.length > 2) lecture.section else lecture.subtitle
                if (subtitleToDisplay.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleToDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val roomText = lecture.classes.firstOrNull()?.let { "(${it.buildingCode}) ${it.roomName}" } ?: ""
                if (roomText.isNotEmpty()) {
                    Text(
                        text = roomText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = lecture.classes.joinToString(" ") { it.day.name + " " + (it.begin / 60).toString() + ":" + (it.begin % 60).toString().padStart(2, '0') + "-" + (it.end / 60).toString() + ":" + (it.end % 60).toString().padStart(2, '0') },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.width(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(toLetter(lecture.grade), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(toLetter(lecture.load), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(toLetter(lecture.speech), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInfoClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(stringResource(R.string.more), style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.add_course), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun CourseSectionHeader(course: CourseLecture, backgroundColor: Color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = course.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            Row(
                modifier = Modifier.width(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.grade), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.load), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.speech), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun MockView(state: LectureSearchViewModel.ViewState) {
    LectureSearchView(
        navController = rememberNavController(),
        timetableName = "My Table",
        timetableViewModel = PreviewTimetableViewModel(),
        lectureSearchViewModel = PreviewLectureSearchViewModel(initialState = state)
    ) {}
}

@Composable
@Preview(showBackground = true)
private fun LoadedPreview() {
    Theme { MockView(LectureSearchViewModel.ViewState.Loaded) }
}

@Preview
@Composable
private fun LectureRowPreview() {
    Theme {
        LectureRow(
            lecture = Lecture.mock(),
            onClick = {},
            onInfoClick = {},
            onAddClick = {}
        )
    }
}
