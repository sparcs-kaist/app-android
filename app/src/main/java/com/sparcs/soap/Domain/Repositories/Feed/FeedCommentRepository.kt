package com.sparcs.soap.Domain.Repositories.Feed

import com.sparcs.soap.Domain.Enums.FeedReportType
import com.sparcs.soap.Domain.Enums.FeedVoteType
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Domain.Models.Feed.FeedCreateComment
import com.sparcs.soap.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedCommentApi
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.Mocks.mockList
import javax.inject.Inject

interface FeedCommentRepositoryProtocol {
    suspend fun fetchComments(postId: String): List<FeedComment>
    suspend fun writeComment(postId: String, request: FeedCreateComment): FeedComment
    suspend fun writeReply(commentId: String, request: FeedCreateComment): FeedComment
    suspend fun deleteComment(commentId: String)
    suspend fun vote(commentId: String, type: FeedVoteType)
    suspend fun deleteVote(commentId: String)
    suspend fun reportComment(commentId: String, reason: FeedReportType)
}

class FakeFeedCommentRepository : FeedCommentRepositoryProtocol {
    private val mockComments = FeedComment.mockList()
    override suspend fun fetchComments(postId: String): List<FeedComment> {
        return mockComments
    }

    override suspend fun writeComment(
        postId: String,
        request: FeedCreateComment,
    ): FeedComment {
        return FeedComment.mock()
    }

    override suspend fun writeReply(
        commentId: String,
        request: FeedCreateComment,
    ): FeedComment {
        return FeedComment.mock()
    }

    override suspend fun deleteComment(commentId: String) {}
    override suspend fun vote(commentId: String, type: FeedVoteType) {}
    override suspend fun deleteVote(commentId: String) {}
    override suspend fun reportComment(commentId: String, reason: FeedReportType) {}
}

class FeedCommentRepository @Inject constructor(
    private val api: FeedCommentApi,
) : FeedCommentRepositoryProtocol {

    override suspend fun fetchComments(postId: String): List<FeedComment> {
        return api.fetchComments(postId).map { it.toModel() }
    }

    override suspend fun writeComment(postId: String, request: FeedCreateComment): FeedComment {
        val dto = FeedCommentRequestDTO.fromModel(request)
        return api.writeComment(postId, dto).toModel()
    }

    override suspend fun writeReply(commentId: String, request: FeedCreateComment): FeedComment {
        val dto = FeedCommentRequestDTO.fromModel(request)
        return api.writeReply(commentId, dto).toModel()
    }

    override suspend fun deleteComment(commentId: String) {
        api.deleteComment(commentId)
    }

    override suspend fun vote(commentId: String, type: FeedVoteType) {
        api.vote(commentId, mapOf("vote" to type.name))
    }

    override suspend fun deleteVote(commentId: String) {
        api.deleteVote(commentId)
    }

    override suspend fun reportComment(commentId: String, reason: FeedReportType) {
        api.reportComment(commentId, mapOf("reason" to reason.name))
    }
}
