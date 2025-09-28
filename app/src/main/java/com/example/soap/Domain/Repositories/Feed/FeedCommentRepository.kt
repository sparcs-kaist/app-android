package com.example.soap.Domain.Repositories.Feed

import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedCreateComment
import com.example.soap.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import com.example.soap.Networking.RetrofitAPI.Feed.FeedCommentApi
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import javax.inject.Inject

interface FeedCommentRepositoryProtocol {
    suspend fun fetchComments(postId: String): List<FeedComment>
    suspend fun writeComment(postId: String, request: FeedCreateComment): FeedComment
    suspend fun writeReply(commentId: String, request: FeedCreateComment): FeedComment
    suspend fun deleteComment(commentId: String)
    suspend fun vote(commentId: String, type: FeedVoteType)
    suspend fun deleteVote(commentId: String)
}

class FakeFeedCommentRepository: FeedCommentRepositoryProtocol {
        private val mockComments = FeedComment.mockList()
        override suspend fun fetchComments(postId: String): List<FeedComment> {
            return mockComments
        }
        override suspend fun writeComment(
            postId: String,
            request: FeedCreateComment
        ): FeedComment {
            return FeedComment.mock()
        }
        override suspend fun writeReply(
            commentId: String,
            request: FeedCreateComment
        ): FeedComment {
            return FeedComment.mock()
        }
        override suspend fun deleteComment(commentId: String) {}
        override suspend fun vote(commentId: String, type: FeedVoteType) {}
        override suspend fun deleteVote(commentId: String) {}
}

class FeedCommentRepository @Inject constructor(
    private val api: FeedCommentApi
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
}
