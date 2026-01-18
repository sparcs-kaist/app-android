package org.sparcs.App.Shared.ViewModelMocks.Feed

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.App.Domain.Models.Feed.FeedComment
import org.sparcs.App.Domain.Models.Feed.FeedPost
import org.sparcs.App.Features.FeedPost.FeedPostViewModel
import org.sparcs.App.Features.FeedPost.FeedPostViewModelProtocol
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.Shared.Mocks.mockList

class MockFeedPostViewModel(initialState: FeedPostViewModel.ViewState) : FeedPostViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedPostViewModel.ViewState> = _state.asStateFlow()

    override val post: FeedPost = FeedPost.mock()

    override var comments: List<FeedComment> = FeedComment.mockList()
    override var text: String = ""
    override var image: Bitmap? = null
    override var isAnonymous: Boolean = false

    override suspend fun fetchComments(postID: String, initial: Boolean) {}
    override suspend fun writeComment(postID: String): FeedComment {
        return FeedComment.mock()
    }
    override suspend fun writeReply(commentID: String): FeedComment {
        return FeedComment.mock()
    }
}
