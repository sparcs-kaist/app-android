package org.sparcs.soap.app.features.taxiChat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import org.sparcs.soap.R

/**
 * Copies [text] and confirms it. Android 13 and later show their own confirmation, so the toast is
 * only for older versions.
 */
internal fun Context.copyChatText(text: String) {
    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(null, text))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(this, R.string.share_copied, Toast.LENGTH_SHORT).show()
    }
}
