package org.sparcs.App.Shared.ViewModelMocks.Ara

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.App.Domain.Models.Ara.AraPost
import org.sparcs.App.Domain.Models.Ara.AraPostComment
import org.sparcs.App.Features.Post.PostViewModel
import org.sparcs.App.Features.Post.PostViewModelProtocol
import org.sparcs.App.Shared.Mocks.mock


class MockPostViewModel(initialState: PostViewModel.ViewState) : PostViewModelProtocol {

    override val state: StateFlow<PostViewModel.ViewState> =
        MutableStateFlow(initialState)
    override val isFoundationModelsAvailable = true

    private val _post = MutableStateFlow(AraPost.mock())
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
    override fun handleException(error: Throwable) {}
}
