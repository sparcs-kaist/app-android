package org.sparcs.soap.app.features.lectureSearch

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.gradeLetter
import org.sparcs.soap.app.domain.helpers.loadLetter
import org.sparcs.soap.app.domain.helpers.speechLetter
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchChrome
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchList
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchViewNavigationBar
import org.sparcs.soap.app.features.navigationBar.Channel
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
            LectureSearchList(
                timetableViewModel = timetableViewModel,
                viewModel = lectureSearchViewModel,
                onOpenLecture = { lecture ->
                    timetableViewModel.setCandidateLecture(lecture.takeUnless { timetableViewModel.selectedTimetable.value?.contains(it) == true })
                    navController.navigate(Channel.LectureDetail.name + "?lecture_json=${Uri.encode(Gson().toJson(lecture))}")
                    onFoldSheet()
                },
                onOpenCourse = { id ->
                    timetableViewModel.setCandidateLecture(null)
                    navController.navigate(Channel.CourseView.name + "?courseId=$id")
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LectureRow(
    lecture: Lecture,
    isSelected: Boolean = false,
    showSection: Boolean = true,
    conflicts: List<String> = emptyList(),
    isAdded: Boolean = false,
    isWishlisted: Boolean = false,
    onToggleWishlist: (() -> Unit)? = null,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(onClick = onClick, onLongClick = onInfoClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LectureRowMainContent(
            lecture = lecture,
            showSection = showSection,
            isWishlisted = isWishlisted,
            onToggleWishlist = onToggleWishlist
        )
        LectureRowMetrics(lecture = lecture)
        LectureRowStatus(isAdded = isAdded, conflicts = conflicts)
        if (isSelected) {
            LectureRowActions(
                isAdded = isAdded,
                onInfoClick = onInfoClick,
                onAddClick = onAddClick
            )
        }
    }
}

@Composable
private fun LectureRowMainContent(
    lecture: Lecture,
    showSection: Boolean,
    isWishlisted: Boolean,
    onToggleWishlist: (() -> Unit)?
) {
    val secondary = LectureSearchChrome.secondary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LectureRowHeader(
                lecture = lecture,
                showSection = showSection
            )
            LectureRowTimeInfo(lecture = lecture, secondary = secondary)
        }

        if (onToggleWishlist != null) {
            IconButton(onClick = onToggleWishlist) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isWishlisted) R.string.wishlist_remove else R.string.wishlist_add
                    ),
                    modifier = Modifier.size(25.dp),
                    tint = if (isWishlisted) MaterialTheme.colorScheme.primary else secondary
                )
            }
        }
    }
}

@Composable
private fun LectureRowHeader(
    lecture: Lecture,
    showSection: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (showSection && lecture.section.isNotBlank() && lecture.section.length <= 2) {
            SectionBadge(section = lecture.section)
        }
        Text(
            text = lecture.professors.joinToString(", ") { it.name }.ifEmpty { stringResource(R.string.unknown) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionBadge(section: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
    ) {
        Text(
            text = section,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LectureRowTimeInfo(
    lecture: Lecture,
    secondary: Color
) {
    val subtitle = if (lecture.section.length > 2) lecture.section else lecture.subtitle
    if (subtitle.isNotBlank()) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = secondary
        )
    }

    val slots = remember(lecture.classes) {
        lecture.classes.groupBy { it.begin to it.end }.entries.sortedBy { it.key.first }
    }
    val times = slots.map { (slot, classes) ->
        val days = classes.map { it.day }.distinct().sortedBy { it.value }
            .map { stringResource(it.stringValue) }.joinToString(", ")
        val begin = "%02d:%02d".format(slot.first / 60, slot.first % 60)
        val end = "%02d:%02d".format(slot.second / 60, slot.second % 60)
        "$days $begin\u2013$end"
    }.joinToString(" \u00b7 ")

    Text(
        text = times.ifEmpty { stringResource(R.string.lecture_no_time) },
        style = MaterialTheme.typography.bodyMedium,
        color = secondary
    )
}

@Composable
private fun LectureRowMetrics(lecture: Lecture) {
    val secondary = LectureSearchChrome.secondary
    val warning = Color(0xFFFF8800)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (lecture.grade == 0.0 && lecture.load == 0.0 && lecture.speech == 0.0) {
            Text(
                text = stringResource(R.string.lecture_no_ratings),
                style = MaterialTheme.typography.bodySmall,
                color = secondary.copy(alpha = 0.55f),
                modifier = Modifier.weight(1f)
            )
        } else {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                LectureRating(stringResource(R.string.grade), lecture.gradeLetter)
                LectureRating(stringResource(R.string.load), lecture.loadLetter)
                LectureRating(stringResource(R.string.speech), lecture.speechLetter)
            }
        }

        if (lecture.capacity > 0) {
            val tint = if (lecture.enrolledCount > lecture.capacity) warning else secondary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.People, null, Modifier.size(16.dp), tint = tint)
                Text(
                    text = stringResource(R.string.lecture_enrollment, lecture.enrolledCount, lecture.capacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = tint
                )
            }
        }
    }
}

@Composable
private fun LectureRowStatus(
    isAdded: Boolean,
    conflicts: List<String>
) {
    if (!isAdded && conflicts.isEmpty()) return

    val warning = Color(0xFFFF8800)
    val tint = if (isAdded) MaterialTheme.colorScheme.primary else warning

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isAdded) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint
        )
        Text(
            text = if (isAdded) {
                stringResource(R.string.lecture_in_timetable)
            } else {
                stringResource(R.string.lecture_conflicts, conflicts.joinToString(", "))
            },
            style = MaterialTheme.typography.bodySmall,
            color = tint
        )
    }
}

@Composable
private fun LectureRowActions(
    isAdded: Boolean,
    onInfoClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onInfoClick,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.heightIn(min = 36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Button(
            onClick = onAddClick,
            enabled = !isAdded,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.heightIn(min = 36.dp)
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isAdded) stringResource(R.string.lecture_in_timetable) else stringResource(R.string.add_course),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun LectureRating(label: String, letter: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LectureSearchChrome.secondary
        )
        Text(
            text = letter,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ratingColor(letter)
        )
    }
}

@Composable
fun CourseSectionHeader(
    course: CourseLecture,
    backgroundColor: Color = LectureSearchChrome.card,
    onOpenCourse: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClickLabel = stringResource(R.string.view_course), onClick = onOpenCourse)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val lecture = course.lectures.firstOrNull()
                val credits = lecture?.let {
                    if (it.credit > 0) "${it.credit} ${stringResource(R.string.credit)}" else "${it.creditAU} ${stringResource(R.string.au)}"
                }
                Text(
                    text = listOfNotNull(course.code, stringResource(course.type.displayName), credits).joinToString(" \u00b7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = LectureSearchChrome.secondary
                )
            }
            if (course.completed) {
                Text(
                    text = stringResource(R.string.course_taken),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = LectureSearchChrome.secondary.copy(alpha = 0.55f)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 15.dp),
            color = LectureSearchChrome.separator,
            thickness = 0.5.dp
        )
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
    Theme { MockView(LectureSearchViewModel.ViewState.Loaded()) }
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

@Preview(showBackground = true)
@Composable
private fun CourseSectionHeaderPreview() {
    Theme { CourseSectionHeader(CourseLecture.mock()) }
}

@Composable
private fun ratingColor(letter: String): Color = when (letter.firstOrNull()) {
    'A' -> Color(0xFF23834D)
    'B' -> Color(0xFF63832B)
    'C' -> Color(0xFF997016)
    'D', 'F' -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
