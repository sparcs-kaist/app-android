package com.example.soap.Features.FeedPostCompose

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.soap.Domain.Models.Feed.FeedUser

data class FeedPostPhotoItem(
    val id: String,
    val image: Bitmap,
    var spoiler: Boolean,
    var description: String,
)

interface FeedPostComposeViewModelProtocol {
    var feedUser: FeedUser?
    var text: String
    var selectedComposeType: FeedPostComposeViewModel.ComposeType
    var selectedItems: List<Uri>
    var selectedImages: List<FeedPostPhotoItem>

    suspend fun fetchFeedUser()
    suspend fun writePost()
    suspend fun loadImagesAndReconcile(context: Context)
    fun removeImage(index: Int)
}