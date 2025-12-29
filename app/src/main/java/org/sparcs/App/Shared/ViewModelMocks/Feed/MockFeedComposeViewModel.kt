package org.sparcs.App.Shared.ViewModelMocks.Feed

import android.content.Context
import android.net.Uri
import org.sparcs.App.Domain.Enums.Feed.FeedPostPhotoItem
import org.sparcs.App.Domain.Models.Feed.FeedUser
import org.sparcs.App.Features.FeedPostCompose.FeedPostComposeViewModel
import org.sparcs.App.Features.FeedPostCompose.FeedPostComposeViewModelProtocol


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