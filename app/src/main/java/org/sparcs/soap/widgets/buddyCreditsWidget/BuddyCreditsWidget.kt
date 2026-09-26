package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.widgets.WidgetEntryPoint
import org.sparcs.soap.widgets.theme.ui.WidgetTheme

class BuddyCreditsWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tokenStorage = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .tokenStorage()
        val signInRequired = tokenStorage.getAccessToken() == null
        val snapshot = if (signInRequired) null else CreditSummarySnapshotStore(context).snapshot
        provideContent {
            WidgetTheme { CreditsWidgetView(snapshot, signInRequired) }
        }
    }
}

class BuddyCreditsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BuddyCreditsWidget()
}
