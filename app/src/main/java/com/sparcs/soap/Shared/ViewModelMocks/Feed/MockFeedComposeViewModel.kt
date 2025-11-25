package com.sparcs.soap.Shared.ViewModelMocks.Feed

import android.content.Context
import android.net.Uri
import com.sparcs.soap.Domain.Enums.Feed.FeedPostPhotoItem
import com.sparcs.soap.Domain.Models.Feed.FeedUser
import com.sparcs.soap.Features.FeedPostCompose.FeedPostComposeViewModel
import com.sparcs.soap.Features.FeedPostCompose.FeedPostComposeViewModelProtocol


class MockFeedPostComposeViewModel : FeedPostComposeViewModelProtocol {

    override var feedUser: FeedUser? = null
    override var text: String = "Mock text"
    override var selectedComposeType: FeedPostComposeViewModel.ComposeType =
        FeedPostComposeViewModel.ComposeType.Publicly
    override var selectedItems: List<Uri> = emptyList()
    override var selectedImages: List<FeedPostPhotoItem> = emptyList()

    override suspend fun fetchFeedUser() {}
    override suspend fun writePost() {}
    override suspend fun loadImagesAndReconcile(context: Context) {}
    override fun removeImage(index: Int) {}
}