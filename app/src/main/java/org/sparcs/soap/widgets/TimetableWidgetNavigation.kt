package org.sparcs.soap.widgets

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.features.main.MainActivity

internal fun timetableWidgetIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Constants.OTL_SHARE_URL.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra("extra_from_widget", true)
    }
