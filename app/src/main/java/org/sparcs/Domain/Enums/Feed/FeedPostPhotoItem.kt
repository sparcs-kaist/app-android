package org.sparcs.Domain.Enums.Feed

import android.graphics.Bitmap

data class FeedPostPhotoItem(
    val id: String,
    val image: Bitmap,
    var spoiler: Boolean,
    var description: String,
)