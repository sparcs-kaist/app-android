package org.sparcs.soap.App.Shared.ViewModelMocks.Ara

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Features.Post.PostViewModel
import org.sparcs.soap.App.Features.Post.PostViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Ara.mock


class MockPostViewModel(initialState: PostViewModel.ViewState, post: AraPost) : PostViewModelProtocol {

    override val state: StateFlow<PostViewModel.ViewState> =
        MutableStateFlow(initialState)
    override val isFoundationModelsAvailable = true

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    private val _post = MutableStateFlow(post)
    override val post: StateFlow<AraPost?> = _post.asStateFlow()

    override suspend fun fetchPost() {}

    override suspend fun upVote() {}

    override suspend fun downVote() {}

    override suspend fun writeComment(content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment {
        return AraPostComment.mock()
    }

    override suspend fun report(type: AraContentReportType) {}

    override suspend fun summarisedContent(): String {
        return ""
    }

    override suspend fun deletePost() {}
    override suspend fun toggleBookmark() {}
    override suspend fun upVoteComment(comment: AraPostComment) {}
    override suspend fun downVoteComment(comment: AraPostComment) {}
    override suspend fun reportComment(commentID: Int, type: AraContentReportType) {}
    override suspend fun deleteComment(comment: AraPostComment) {}
}
