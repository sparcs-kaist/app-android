package org.sparcs.soap.app.shared.viewModelMocks.feed

import android.net.Uri
import org.sparcs.soap.app.domain.enums.feed.FeedPostPhotoItem
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.features.feedPostCompose.FeedPostComposeViewModel
import org.sparcs.soap.app.features.feedPostCompose.FeedPostComposeViewModelProtocol


class MockFeedPostComposeViewModel : FeedPostComposeViewModelProtocol {

    override var feedUser: FeedUser? = null
    override var text: String = "Mock text"
    override var selectedComposeType: FeedPostComposeViewModel.ComposeType =
        FeedPostComposeViewModel.ComposeType.Publicly
    override var selectedItems: List<Uri> = emptyList()
    override var selectedImages: List<FeedPostPhotoItem> = emptyList()
    override val alertState: AlertState? = null
    override var isAlertPresented: Boolean = false
    override var isUploading: Boolean = false

    override fun fetchFeedUser() {}
    override suspend fun submitPost(): Boolean { return true }

    override fun removeImage(index: Int) {}
    override fun handleException(error: Throwable) {}
}