package org.sparcs.soap.App.Shared.ViewModelMocks.Feed

import android.content.Context
import android.net.Uri
import org.sparcs.soap.App.Domain.Enums.Feed.FeedPostPhotoItem
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Features.FeedPostCompose.FeedPostComposeViewModel
import org.sparcs.soap.App.Features.FeedPostCompose.FeedPostComposeViewModelProtocol


class MockFeedPostComposeViewModel : FeedPostComposeViewModelProtocol {

    override var feedUser: FeedUser? = null
    override var text: String = "Mock text"
    override var selectedComposeType: FeedPostComposeViewModel.ComposeType =
        FeedPostComposeViewModel.ComposeType.Publicly
    override var selectedItems: List<Uri> = emptyList()
    override var selectedImages: List<FeedPostPhotoItem> = emptyList()

    override fun fetchFeedUser() {}
    override suspend fun writePost() {}
    override suspend fun loadImagesAndReconcile(context: Context) {}
    override fun removeImage(index: Int) {}
    override fun handleException(error: Throwable) {}
}