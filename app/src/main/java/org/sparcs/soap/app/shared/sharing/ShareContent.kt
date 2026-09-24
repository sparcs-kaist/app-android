package org.sparcs.soap.app.shared.sharing

import android.net.Uri
import androidx.annotation.StringRes
import org.sparcs.soap.R

data class ShareContent(
    val title: String,
    val text: String,
    val topColor: String,
    val bottomColor: String,
    val link: String? = null,
    val copyText: String? = null,
    @param:StringRes val copyLabel: Int = R.string.share_copy_image,
)

enum class ShareTarget(@param:StringRes val label: Int, val packageName: String? = null) {
    Instagram(R.string.theme_share_instagram_story, "com.instagram.android"),
    Messages(R.string.share_messages),
    Feed(R.string.theme_share_feed),
    Copy(R.string.share_copy_image),
    More(R.string.share_more),
}

data class ShareRequest(val target: ShareTarget, val content: ShareContent, val imageUri: Uri?)
