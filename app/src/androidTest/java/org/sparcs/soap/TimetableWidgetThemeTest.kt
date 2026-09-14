package org.sparcs.soap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableLargeWidgetView
import org.sparcs.soap.widgets.buddyTimetableWidget.toWidgetUiState
import org.sparcs.soap.widgets.themed
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class TimetableWidgetThemeTest {
    private val theme = TimetableTheme.builtIn.first { it.id == "builtin.ocean" }
        .copy(backgroundColorHex = "0B1622", separatorColorHex = "3C6E91", gridLabelColorHex = "9FD3E8")
    private val lecture = Lecture.mockList().first()
        .copy(name = "Algorithms", subtitle = "", courseID = 3, classes = listOf(LectureClass(DayType.MON, 540, 630, "E11", "Building", "101")))
    private val table = Timetable("12", listOf(lecture), listOf(TimetableActivity(17, "Study group", "Library", 2, 660, 750)))

    @Test fun widgetRendersTheSelectedThemeBackgroundAndPalette() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val size = DpSize(360.dp, 400.dp)
        val state = table.toWidgetUiState(theme).timetable!!
        val remoteViews = GlanceRemoteViews().compose(context, size) {
            WidgetTheme("Light") {
                Box(GlanceModifier.fillMaxSize().background(theme.backgroundColor!!)) {
                    TimetableLargeWidgetView(state.themed(theme), theme)
                }
            }
        }.remoteViews

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = remoteViews.apply(context, FrameLayout(context))
            val density = context.resources.displayMetrics.density
            val width = (size.width.value * density).toInt()
            val height = (size.height.value * density).toInt()
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(context.getExternalFilesDir(null), "widget-theme.png").outputStream()
                .use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

            // The grid sits on the theme background instead of the widget's own surface color.
            assertEquals(theme.backgroundColor!!.toArgb(), bitmap.getPixel(width / 2, height - 2))

            // Monday 09:00 holds the lecture, drawn in its palette slot.
            val gridLeft = (28 * density).roundToInt()
            val dayWidth = (width - gridLeft) / 5f
            val mondayX = (gridLeft + 0.5f * dayWidth).roundToInt()
            assertTrue(
                "Lecture cell missing from the themed widget",
                (0 until height).any { bitmap.getPixel(mondayX, it) == theme.colorFor(3).toArgb() }
            )

            // Wednesday 11:00 holds the activity, which shares the same palette.
            val wednesdayX = (gridLeft + 2.5f * dayWidth).roundToInt()
            assertTrue(
                "Activity cell missing from the themed widget",
                (0 until height).any { bitmap.getPixel(wednesdayX, it) == theme.colorFor(17).toArgb() }
            )
            bitmap.recycle()
        }
    }
}
