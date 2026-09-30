package org.sparcs.soap

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationContent
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditRequirementsContent
import org.sparcs.soap.app.features.timetable.creditCalculation.GradeEntryView
import org.sparcs.soap.app.features.timetable.creditCalculation.creditPreviewState
import org.sparcs.soap.app.theme.ui.Theme
import java.io.File

class CreditCalculationUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test fun selectingAndClearingGradeUpdatesTheRow() {
        val initial = creditPreviewState()
        val semester = initial.semesters.first()
        val lecture = initial.timetables.getValue(semester.id).lectures.first()
        var selected: LectureGrade? = LectureGrade.A_MINUS
        compose.activity.setContent {
            var state by remember { mutableStateOf(initial.copy(timetables = mapOf(
                semester.id to initial.timetables.getValue(semester.id).copy(lectures = listOf(lecture))))) }
            Theme {
                GradeEntryView(semester, state, {}, { grade, id ->
                    selected = grade
                    state = state.copy(grades = if (grade == null) state.grades - id else state.grades + (id to grade))
                })
            }
        }
        val description = text(R.string.credit_grade_for, lecture.name)
        compose.onNodeWithContentDescription(description).performScrollTo().performClick()
        compose.onNodeWithText("A+").performClick()
        compose.runOnIdle { assertEquals(LectureGrade.A_PLUS, selected) }
        compose.onNodeWithContentDescription(description).performClick()
        compose.onNodeWithText(text(R.string.credit_clear_grade)).performClick()
        compose.runOnIdle { assertEquals(null, selected) }
        compose.onNodeWithText(text(R.string.credit_enter_grade)).assertIsDisplayed()
        capture("credit-grade-entry.png")
    }

    @Test fun requirementsEditorCanBeCancelledWithoutSaving() {
        var saved: CreditRequirements? = null
        compose.activity.setContent {
            Theme { CreditRequirementsContent(creditPreviewState(), {}, { saved = it }) }
        }
        compose.onAllNodesWithText(text(R.string.credit_edit)).onFirst().performClick()
        compose.onNodeWithText(text(R.string.credit_cancel)).performClick()
        compose.runOnIdle { assertEquals(null, saved) }
        capture("credit-requirements.png")
    }

    @Test fun summaryOpensRequirements() {
        var opened = false
        compose.activity.setContent {
            Theme { CreditCalculationContent(creditPreviewState(), {}, {}, {}, { opened = true }) }
        }
        compose.onNodeWithText(text(R.string.credit_requirements)).performClick()
        compose.runOnIdle { assertEquals(true, opened) }
        capture("credit-summary.png")
    }

    private fun capture(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
