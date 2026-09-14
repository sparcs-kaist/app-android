package org.sparcs.soap

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.features.timetable.components.CompactTimetableSelector
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

class TimetableSelectorUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun showSelector(name: String = "Plan A") {
        compose.runOnUiThread {
            compose.activity.setContent {
                Theme { CompactTimetableSelector(viewModel = PreviewTimetableViewModel(), timetableName = name) }
            }
        }
    }

    private fun openDeleteConfirmation() {
        compose.onNodeWithContentDescription("Menu").performClick()
        // While the menu is open it holds the only "Delete" on screen; the dialog's own comes after.
        compose.onAllNodesWithText(text(R.string.timetable_delete)).onFirst().performClick()
    }

    @Test fun deletingATableAsksForConfirmationFirst() {
        showSelector()

        openDeleteConfirmation()
        compose.onNodeWithText(text(R.string.timetable_delete_confirm_title, "Plan A")).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.timetable_delete_confirm_message)).assertIsDisplayed()

        compose.onNodeWithText(text(R.string.cancel)).performClick()
        compose.onNodeWithText(text(R.string.timetable_delete_confirm_title, "Plan A")).assertDoesNotExist()

        openDeleteConfirmation()
        compose.onNodeWithText(text(R.string.delete)).performClick()
        compose.onNodeWithText(text(R.string.timetable_delete_confirm_title, "Plan A")).assertDoesNotExist()
    }

    @Test fun theMenuOffersDuplicatingMyTable() {
        showSelector()

        compose.onNodeWithContentDescription("Menu").performClick()
        compose.onNodeWithText(text(R.string.timetable_add)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.timetable_duplicate)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.timetable_duplicate)).performClick()
        compose.onNodeWithText(text(R.string.timetable_duplicate)).assertDoesNotExist()
    }
}
