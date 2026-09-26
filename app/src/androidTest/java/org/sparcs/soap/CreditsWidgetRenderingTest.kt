package org.sparcs.soap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.widgets.buddyCreditsWidget.CreditsWidgetView
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableSmallWidgetView
import org.sparcs.soap.widgets.buddyTimetableWidget.WidgetTimetableEntry
import org.sparcs.soap.widgets.buddyTimetableWidget.mock
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import java.io.File

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class CreditsWidgetRenderingTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun creditsWidgetRendersSmallWideAndEmptyStates() = runBlocking {
        for (width in listOf(170, 320)) {
            val labels = render("credits-widget-$width.png", DpSize(width.dp, 170.dp)) {
                CreditsWidgetView(CreditSummarySnapshot(3.73, 96, 138))
            }
            assertTrue(labels.any { it.contains("96") && it.contains("138") })
            assertTrue(labels.any { it.contains("3.73") })
        }
        val empty = render("credits-widget-empty.png", DpSize(170.dp, 170.dp)) { CreditsWidgetView(null) }
        assertTrue(empty.contains(context.getString(R.string.credit_widget_open)))
    }

    @Test fun smallTimetableRetainsThemeAndLectureBlocks() = runBlocking {
        val theme = TimetableTheme.Default.copy(backgroundColorHex = "102030", gridLabelColorHex = "FFFFFF")
        render("timetable-small.png", DpSize(170.dp, 170.dp), verify = { bitmap ->
            val color = theme.backgroundColor!!.toArgb()
            assertTrue(bitmap.getPixel(0, 0) == color)
            assertTrue((24 until bitmap.height).any { bitmap.getPixel(bitmap.width / 10, it) != color })
        }) {
            Box(GlanceModifier.fillMaxSize().background(theme.backgroundColor!!)) {
                TimetableSmallWidgetView(WidgetTimetableEntry.mock(), theme)
            }
        }
    }

    private suspend fun render(
        name: String,
        size: DpSize,
        verify: (Bitmap) -> Unit = {},
        content: @Composable () -> Unit,
    ): List<String> {
        val views = GlanceRemoteViews().compose(context, size) { WidgetTheme { content() } }.remoteViews
        var labels = emptyList<String>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = views.apply(context, FrameLayout(context))
            val density = context.resources.displayMetrics.density
            val width = (size.width.value * density).toInt()
            val height = (size.height.value * density).toInt()
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(context.getExternalFilesDir(null), name).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            labels = textLabels(view)
            verify(bitmap)
            bitmap.recycle()
        }
        return labels
    }

    private fun textLabels(view: View): List<String> = when (view) {
        is TextView -> listOf(view.text.toString())
        is ViewGroup -> (0 until view.childCount).flatMap { textLabels(view.getChildAt(it)) }
        else -> emptyList()
    }
}
