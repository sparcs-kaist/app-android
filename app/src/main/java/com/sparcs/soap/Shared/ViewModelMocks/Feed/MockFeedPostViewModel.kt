package com.sparcs.soap.Shared.ViewModelMocks.Feed

import android.graphics.Bitmap
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Features.FeedPost.FeedPostViewModel
import com.sparcs.soap.Features.FeedPost.FeedPostViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockFeedPostViewModel(initialState: FeedPostViewModel.ViewState) : FeedPostViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedPostViewModel.ViewState> = _state.asStateFlow()

    override val post: FeedPost = FeedPost.mock()

    override var comments: List<FeedComment> = FeedComment.mockList()
    override var text: String = ""
    override var image: Bitmap? = null
    override var isAnonymous: Boolean = false

    override suspend fun fetchComments(postID: String) {}
    override suspend fun writeComment(postID: String): FeedComment {
        return FeedComment.mock()
    }
    override suspend fun writeReply(commentID: String): FeedComment {
        return FeedComment.mock()
    }
    override fun handleException(error: Throwable) {}
}
