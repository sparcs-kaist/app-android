package org.sparcs.soap

import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetablePlacement
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareSheet
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareSnapshot
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.sharing.ShareImageStore
import org.sparcs.soap.app.shared.sharing.ShareViewModel
import org.sparcs.soap.app.theme.ui.Theme
import java.io.File

class TimetableSharingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    @Test fun sheetCopiesFullResolutionProfessorImageAndDismisses() {
        val lecture = Lecture.mock().let {
            it.copy(name = "Shared Lecture", subtitle = "", professors = listOf(Professor(1, "Professor Only")),
                classes = listOf(it.classes.first().copy(begin = 540, end = 660, roomName = "DO_NOT_EXPORT_ROOM")))
        }
        val model = ShareViewModel(ShareImageStore(context))
        val show = mutableStateOf(true)
        compose.runOnUiThread {
            compose.activity.setContent {
                Theme {
                    if (show.value) TimetableShareSheet(
                        TimetableShareSnapshot(Semester.mockList().first(), Timetable("share", listOf(lecture)), TimetableTheme.Default),
                        onDismiss = { show.value = false },
                        onFeed = { _, _ -> },
                        viewModel = model,
                    )
                }
            }
        }
        compose.onNodeWithText("Professor Only").assertExists()
        compose.onNodeWithText("DO_NOT_EXPORT_ROOM", substring = true).assertDoesNotExist()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        File(context.getExternalFilesDir(null), "timetable-share-sheet.png").outputStream().use {
            instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val clipboard = compose.activity.getSystemService(ClipboardManager::class.java)
        val previousUri = clipboard.primaryClip?.getItemAt(0)?.uri
        compose.onNodeWithText(text(R.string.share_copy_image)).performScrollTo().performClick()
        compose.waitUntil(30_000) {
            compose.mainClock.advanceTimeByFrame()
            clipboard.primaryClip?.getItemAt(0)?.uri?.let { it != previousUri && it.path?.contains("shared_images") == true } == true
        }
        val uri = clipboard.primaryClip!!.getItemAt(0).uri
        val bitmap = context.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it) }
        assertNotNull(bitmap)
        assertEquals(1170, bitmap.width)
        assertEquals(2532, bitmap.height)
        File(context.getExternalFilesDir(null), "timetable-share-export.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        pressBack()
        compose.waitForIdle()
        assertFalse(show.value)
        model.reset()
    }

    @Test fun renderedGridRetainsWeekendAndMidnightActivity() {
        compose.runOnUiThread {
            compose.activity.setContent {
                Theme {
                    Box(Modifier.fillMaxWidth().height(550.dp)) {
                        TimetableGrid(Timetable("weekend", emptyList(), listOf(TimetableActivity(7, "Late study", "", 6, 1380, 1440))), placement = TimetablePlacement.Render)
                    }
                }
            }
        }
        compose.onNodeWithText(text(R.string.sun)).assertExists()
        compose.onNodeWithText("Late study").assertExists()
    }
    @Test fun savedImagesHaveUniqueReadableUris() = runBlocking {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val store = ShareImageStore(context)
        val first = store.save(bitmap)
        val second = store.save(bitmap)
        assertNotEquals(first, second)
        context.contentResolver.openInputStream(first)!!.use { assertTrue(it.readBytes().isNotEmpty()) }
        context.contentResolver.openInputStream(second)!!.use { assertTrue(it.readBytes().isNotEmpty()) }
    }

}
