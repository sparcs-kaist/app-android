package org.sparcs.soap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.widgets.buddyTimetableWidget.*
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import java.io.File

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class ActivityWidgetTest {
    @Test fun rendersActivitiesAlongsideClassesAtDifferentWidgetSizes() = runBlocking {
        val lecture = Lecture.mockList().first().copy(name = "Algorithms", subtitle = "", classes = listOf(LectureClass(DayType.MON, 540, 630, "E11", "Building", "101")))
        val table = Timetable("12", listOf(lecture), listOf(
            TimetableActivity(17, "Study group", "Library", 0, 660, 750),
            TimetableActivity(18, "Workout", "Gym", 2, 960, 1020),
            TimetableActivity(19, "Dinner", "Dorm", 4, 1020, 1080)
        ))
        render(table, DpSize(360.dp, 400.dp), "activity-widget") { views ->
            listOf("Algorithms", "Study group", "Library", "Workout", "Dinner").forEach { title -> assertTrue("Missing $title", views.any { it.text.toString() == title }) }
        }
        render(table, DpSize(240.dp, 240.dp), "activity-widget-small") { views ->
            assertTrue(views.any { it.text.toString() == "Study group" })
        }
    }

    @Test fun fullDayWidgetRendersPastTenHoursAndIncludesSunday() = runBlocking {
        val table = Timetable("12", emptyList(), listOf(
            TimetableActivity(17, "Early", "", 0, 0, 60),
            TimetableActivity(18, "Late", "", 6, 1380, 1440)
        ))
        render(table, DpSize(360.dp, 480.dp), "activity-widget-full-day") { views ->
            assertTrue(views.any { it.text.toString() == "23" })
            assertTrue(views.any { it.text.toString() == "Late" })
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            assertTrue(views.any { it.text.toString() == context.getString(R.string.sun) })
        }
    }

    private suspend fun render(table: Timetable, size: DpSize, name: String, check: (List<TextView>) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val state = table.toWidgetUiState().timetable!!
        val remoteViews = GlanceRemoteViews().compose(context, size) {
            WidgetTheme("Light") { TimetableLargeWidgetView(state) }
        }.remoteViews
        instrumentation.runOnMainSync {
            val view = remoteViews.apply(context, FrameLayout(context))
            val density = context.resources.displayMetrics.density
            val width = (size.width.value * density).toInt()
            val height = (size.height.value * density).toInt()
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, width, height)
            val textViews = mutableListOf<TextView>()
            fun visit(node: View) {
                if (node is TextView) textViews += node
                if (node is ViewGroup) for (index in 0 until node.childCount) visit(node.getChildAt(index))
            }
            visit(view)
            check(textViews)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            view.draw(canvas)
            File(context.getExternalFilesDir(null), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
