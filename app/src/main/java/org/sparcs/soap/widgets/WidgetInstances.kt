package org.sparcs.soap.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.sparcs.soap.widgets.buddyCreditsWidget.BuddyCreditsWidget
import org.sparcs.soap.widgets.buddyCreditsWidget.BuddyCreditsWidgetReceiver
import org.sparcs.soap.widgets.buddyDDayWidget.BuddyDDayWidget
import org.sparcs.soap.widgets.buddyDDayWidget.BuddyDDayWidgetReceiver
import org.sparcs.soap.widgets.buddyTimetableWidget.BuddySilhouetteWidget
import org.sparcs.soap.widgets.buddyTimetableWidget.BuddySilhouetteWidgetReceiver
import org.sparcs.soap.widgets.buddyTimetableWidget.BuddyTimetableWidgetReceiver
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidget
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.BuddyUpcomingClassWidget
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.BuddyUpcomingClassWidgetReceiver

private fun widgetReceiver(widget: Class<out GlanceAppWidget>): Class<out GlanceAppWidgetReceiver> =
    when (widget) {
        BuddyCreditsWidget::class.java -> BuddyCreditsWidgetReceiver::class.java
        BuddyDDayWidget::class.java -> BuddyDDayWidgetReceiver::class.java
        TimetableWidget::class.java -> BuddyTimetableWidgetReceiver::class.java
        BuddySilhouetteWidget::class.java -> BuddySilhouetteWidgetReceiver::class.java
        BuddyUpcomingClassWidget::class.java -> BuddyUpcomingClassWidgetReceiver::class.java
        else -> error("Unknown widget: $widget")
    }

internal fun installedWidgetIds(context: Context, widget: Class<out GlanceAppWidget>): List<GlanceId> {
    val manager = GlanceAppWidgetManager(context)
    return AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, widgetReceiver(widget)))
        .map { manager.getGlanceIdBy(it) }
}

internal suspend fun GlanceAppWidget.updateInstalledWidgets(context: Context) {
    installedWidgetIds(context, javaClass).forEach { update(context, it) }
}

internal fun Activity.ownsAppWidget(appWidgetId: Int): Boolean =
    appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
        AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.configure == componentName
