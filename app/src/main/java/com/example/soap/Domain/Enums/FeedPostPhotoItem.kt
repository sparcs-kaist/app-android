package com.example.soap.Domain.Enums

import android.graphics.Bitmap

data class FeedPostPhotoItem(
    val id: String,
    val image: Bitmap,
    var spoiler: Boolean,
    var description: String,
)