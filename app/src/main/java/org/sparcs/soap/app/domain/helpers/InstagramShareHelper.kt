package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.R
import timber.log.Timber

object InstagramShareHelper {

    fun shareToInstagramStory(
        context: Context,
        imageUri: Uri,
        topColor: Color? = null,
        bottomColor: Color? = null,
    ) {
        val topHex = topColor?.let { color ->
            String.format("#%06X", 0xFFFFFF and color.toArgb())
        } ?: "#121826"
        val bottomHex = bottomColor?.let { color ->
            String.format("#%06X", 0xFFFFFF and color.toArgb())
        } ?: "#000000"

        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            type = "image/png"
            putExtra("interactive_asset_uri", imageUri)
            putExtra("top_background_color", topHex)
            putExtra("bottom_background_color", bottomHex)
            putExtra("source_application", BuildConfig.META_APP_ID)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.grantUriPermission(
            "com.instagram.android",
            imageUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.theme_share_instagram_not_installed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to share to Instagram Story")
            Toast.makeText(
                context,
                context.getString(R.string.theme_share_instagram_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
