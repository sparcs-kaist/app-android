package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedCommentApi
import retrofit2.HttpException
import javax.inject.Inject

interface FeedCommentRepositoryProtocol {
    suspend fun fetchComments(postID: String): List<FeedComment>
    suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment
    suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment
    suspend fun deleteComment(commentID: String)
    suspend fun vote(commentID: String, type: FeedVoteType)
    suspend fun deleteVote(commentID: String)
    suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String)
}

class FeedCommentRepository @Inject constructor(
    private val api: FeedCommentApi,
    private val gson: Gson = Gson(),
) : FeedCommentRepositoryProtocol {

    override suspend fun fetchComments(postID: String): List<FeedComment> = try {
        api.fetchComments(postID).map { it.toModel() }
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment {
        try {
            val dto = FeedCommentRequestDTO.fromModel(request)
            return api.writeComment(postID, dto).toModel()

        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment {
        try {
            val dto = FeedCommentRequestDTO.fromModel(request)
            return api.writeReply(commentID, dto).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deleteComment(commentID: String) {
        try {
            val response = api.deleteComment(commentID)

            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun vote(commentID: String, type: FeedVoteType) = try {
        api.vote(commentID, mapOf("vote" to type.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun deleteVote(commentID: String) = try {
        api.deleteVote(commentID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String) = try {
        api.reportComment(commentID, mapOf("reason" to reason.name, "detail" to detail))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}