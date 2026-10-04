package org.sparcs.soap

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.features.course.components.CourseHistorySection
import org.sparcs.soap.app.features.lectureDetail.LectureDetailContent
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.features.lectureSearch.*
import org.sparcs.soap.app.features.lectureSearch.components.LectureCollisionDialog
import org.sparcs.soap.app.features.lectureSearch.components.LectureTimeRangeContent
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.*

class LectureLayoutRegressionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)
    private fun show(content: @Composable () -> Unit) {
        compose.runOnUiThread { compose.activity.setContent { Theme { content() } } }
    }

    @Test fun wideInspectorHasOneHeaderAndCanHideAndRestoreTimetable() {
        val session = LectureSearchSession(SavedStateHandle()).apply { showPreview(true) }
        show {
            val nav = rememberNavController()
            val table = remember { PreviewTimetableViewModel() }
            LectureSearchPage(
                navController = nav,
                timetableViewModel = table,
                viewModel = remember { PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded()) },
                session = session,
                inspector = {
                    LectureDetailContent(
                        viewModel = remember { PreviewLectureDetailViewModel(LectureDetailViewModel.ViewState.Loaded) },
                        timetableViewModel = table,
                        navController = nav,
                        isSearchContext = true,
                        navigationActions = { LecturePreviewToggle(session) },
                    )
                },
            )
        }
        compose.onNodeWithText(text(R.string.add_to_timetable, "My Table")).assertDoesNotExist()
        compose.onNodeWithContentDescription(text(R.string.hide_timetable_preview)).performClick()
        compose.runOnIdle { assertEquals(false, session.showPreview) }
        compose.onNodeWithContentDescription(text(R.string.show_timetable_preview)).performClick()
        compose.runOnIdle { assertEquals(true, session.showPreview) }
    }

    @Test fun selectingProfessorKeepsHistoryPositionAndShowsSubtitle() {
        val history = (2026 downTo 2020).map { year ->
            CourseHistory.mock().copy(year = year, classes = listOf(CourseHistoryClass(year, "Subtitle $year", "A", listOf(Professor(1, "Professor")))))
        }
        show {
            var selected by remember { mutableStateOf<Int?>(null) }
            Box(Modifier.width(400.dp)) { CourseHistorySection(history, selected) { selected = it } }
        }
        val list = compose.onNode(hasScrollToIndexAction())
        list.performScrollToIndex(5)
        val card = compose.onNodeWithText("Subtitle 2021")
        card.assertIsDisplayed()
        val before = card.fetchSemanticsNode().boundsInRoot
        card.performClick()
        compose.waitForIdle()
        card.assertIsDisplayed()
        assertEquals(before, card.fetchSemanticsNode().boundsInRoot)
    }

    @Test fun overlappingLectureRequiresExplicitReplaceAndAllowsCancel() {
        var replacements = 0
        var dismissals = 0
        show {
            LectureCollisionDialog(Lecture.mock(), listOf("Existing class"), { replacements++ }, { dismissals++ })
        }
        compose.onNodeWithText(text(R.string.cancel)).performClick()
        compose.runOnIdle { assertEquals(0, replacements); assertEquals(1, dismissals) }
        compose.onNodeWithText(text(R.string.replace_overlapping_lecture)).performClick()
        compose.runOnIdle { assertEquals(1, replacements) }
    }

    @Test fun timeRangeCanReachTheLastHalfHour() {
        var selected = LectureTimeFilter()
        show {
            var time by remember { mutableStateOf(LectureTimeFilter()) }
            LectureTimeRangeContent(null, time, { time = it; selected = it }, {})
        }
        compose.onNodeWithText("23", useUnmergedTree = true).assertIsDisplayed()
        val selector = compose.onNode(hasContentDescription(text(R.string.lecture_time_filter), substring = true))
        selector.performTouchInput { click(androidx.compose.ui.geometry.Offset(width * 0.4f, height - 4f)) }
        compose.runOnIdle { assertEquals(1380, selected.end) }
    }
}
