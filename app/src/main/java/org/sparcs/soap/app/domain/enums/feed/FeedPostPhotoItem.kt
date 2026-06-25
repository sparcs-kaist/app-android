package org.sparcs.soap.app.domain.enums.feed

import android.graphics.Bitmap

data class FeedPostPhotoItem(
    val id: String,
    val image: Bitmap,
    var spoiler: Boolean,
    var description: String,
)