package org.sparcs.soap.app.shared.sharing

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.widget.Toast
import androidx.core.content.IntentCompat
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.InstagramShareHelper

class ShareLauncher(private val context: Context) {
    fun launch(request: ShareRequest, onFeed: ((Uri, String) -> Unit)?): Boolean {
        if (request.target == ShareTarget.Copy) {
            val content = request.content
            val text = content.link ?: content.copyText
            val clip = if (text != null) ClipData.newPlainText(content.title, text)
                else request.imageUri?.let { ClipData(content.title, arrayOf("image/png", "text/uri-list"), ClipData.Item(it)) } ?: return false
            return try {
                context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
                Toast.makeText(context, R.string.share_copied, Toast.LENGTH_SHORT).show()
                true
            } catch (_: RuntimeException) { false }
        }
        if (request.target == ShareTarget.Feed && onFeed != null) {
            val uri = request.imageUri ?: return false
            onFeed(uri, request.content.text)
            return true
        }
        if (request.target == ShareTarget.Instagram && request.imageUri != null) {
            val intent = InstagramShareHelper.storyIntent(request.imageUri, request.content.topColor, request.content.bottomColor, request.content.link)
            if (tryStart(intent, grantTo = ShareTarget.Instagram.packageName)) return true
        } else {
            val packageName = when (request.target) {
                ShareTarget.Messages -> Telephony.Sms.getDefaultSmsPackage(context)
                else -> request.target.packageName
            }
            if (packageName != null && tryStart(sendIntent(request).setPackage(packageName))) return true
        }
        return tryStart(Intent.createChooser(sendIntent(request), request.content.title))
    }

    internal fun sendIntent(request: ShareRequest) = Intent(Intent.ACTION_SEND).apply {
        type = if (request.imageUri != null) "image/png" else "text/plain"
        putExtra(Intent.EXTRA_TITLE, request.content.title)
        putExtra(Intent.EXTRA_TEXT, listOfNotNull(request.content.text, request.content.link).joinToString("\n"))
        request.imageUri?.let { uri ->
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(request.content.title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun tryStart(intent: Intent, grantTo: String? = null): Boolean {
        return try {
            if (grantTo != null) {
                if (intent.resolveActivity(context.packageManager) == null) return false
                val uri = IntentCompat.getParcelableExtra(intent, "interactive_asset_uri", Uri::class.java)
                if (uri != null) context.grantUriPermission(grantTo, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
