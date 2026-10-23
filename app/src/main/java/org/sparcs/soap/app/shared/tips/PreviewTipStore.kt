package org.sparcs.soap.app.shared.tips

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.Clock

@Composable
fun rememberPreviewTipStore(hasTables: Boolean = false): TipStore {
    val context = LocalContext.current
    return remember(context, hasTables) {
        val preferences = context.getSharedPreferences("buddy_tips_preview", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        TipStore(preferences, Clock.systemDefaultZone()).apply {
            showsTipsImmediately = true
            checkForExistingTables(hasTables)
        }
    }
}
