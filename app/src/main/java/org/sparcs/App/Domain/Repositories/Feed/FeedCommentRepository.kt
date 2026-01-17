package org.sparcs.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.App.Domain.Enums.Feed.FeedDeletionError
import org.sparcs.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.App.Domain.Models.Feed.FeedComment
import org.sparcs.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.App.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.Feed.FeedCommentApi
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.Shared.Mocks.mockList
import retrofit2.HttpException
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

class FeedCommentRepository @Inject constructor(
    private val api: FeedCommentApi,
    private val gson: Gson = Gson(),
) : FeedCommentRepositoryProtocol {

    override suspend fun fetchComments(postId: String): List<FeedComment> = try {
        api.fetchComments(postId).map { it.toModel() }
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writeComment(postId: String, request: FeedCreateComment): FeedComment {
        try {
            val dto = FeedCommentRequestDTO.fromModel(request)
            return api.writeComment(postId, dto).toModel()

        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun writeReply(commentId: String, request: FeedCreateComment): FeedComment {
        try {
            val dto = FeedCommentRequestDTO.fromModel(request)
            return api.writeReply(commentId, dto).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deleteComment(commentId: String) {
        try {
            val response = api.deleteComment(commentId)
            if (response.isSuccessful) {
                return
            } else {
                if (response.code() == 409) throw FeedDeletionError.HasReplies()
                throw HttpException(response)
            }
        } catch (e: Exception) {
            if (e is FeedDeletionError || e is HttpException) throw e
            handleApiError(gson, e)
        }
    }

    override suspend fun vote(commentId: String, type: FeedVoteType) = try {
        api.vote(commentId, mapOf("vote" to type.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun deleteVote(commentId: String) = try {
        api.deleteVote(commentId)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun reportComment(commentId: String, reason: FeedReportType) = try {
        api.reportComment(commentId, mapOf("reason" to reason.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
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
