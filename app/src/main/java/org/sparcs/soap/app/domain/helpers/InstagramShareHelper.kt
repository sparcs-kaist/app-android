package org.sparcs.soap.app.domain.helpers

import android.content.ClipData
import android.content.Intent
import android.net.Uri

object InstagramShareHelper {
    fun storyIntent(imageUri: Uri, topColor: String, bottomColor: String, contentUrl: String? = null): Intent =
        Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")
            type = "image/png"
            putExtra("interactive_asset_uri", imageUri)
            putExtra("top_background_color", topColor)
            putExtra("bottom_background_color", bottomColor)
            putExtra("source_application", Constants.META_APP_ID)
            contentUrl?.let { putExtra("content_url", it) }
            clipData = ClipData.newRawUri("", imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
