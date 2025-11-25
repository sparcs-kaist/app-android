package com.sparcs.soap.Shared.ViewModelMocks.Ara

import com.sparcs.soap.Domain.Enums.Ara.AraContentReportType
import com.sparcs.soap.Domain.Models.Ara.AraPost
import com.sparcs.soap.Domain.Models.Ara.AraPostComment
import com.sparcs.soap.Features.Post.PostViewModel
import com.sparcs.soap.Features.Post.PostViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


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
