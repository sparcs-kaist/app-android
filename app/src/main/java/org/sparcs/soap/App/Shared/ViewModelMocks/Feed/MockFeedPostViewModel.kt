package org.sparcs.soap.App.Shared.ViewModelMocks.Feed

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Features.FeedPost.FeedPostViewModel
import org.sparcs.soap.App.Features.FeedPost.FeedPostViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList

class MockFeedPostViewModel(initialState: FeedPostViewModel.ViewState) : FeedPostViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedPostViewModel.ViewState> = _state.asStateFlow()

    override val post: FeedPost = FeedPost.mock()

    override var comments: List<FeedComment> = FeedComment.mockList()
    override var text: String = ""
    override var image: Bitmap? = null
    override var isAnonymous: Boolean = false
    override var isSubmittingComment: Boolean = false
    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false
    override val feedUser: FeedUser? = null

    override suspend fun fetchComments(postID: String, initial: Boolean) {}
    override suspend fun submitComment(postID: String, replyingTo: FeedComment?): FeedComment? {
        return null
    }
    override suspend fun reportPost(postID: String, reason: FeedReportType) {}
    override suspend fun fetchFeedUser() {}

    override suspend fun voteComment(comment: FeedComment, type: FeedVoteType?) {}
    override suspend fun deleteComment(comment: FeedComment) {}
    override suspend fun reportComment(commentID: String, reason: FeedReportType) {}

}
