package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
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

    override suspend fun fetchComments(postID: String): List<FeedComment> = safeApiCall(gson) {
        api.fetchComments(postID)
    }.map { it.toModel() }

    override suspend fun writeComment(postID: String, request: FeedCreateComment): FeedComment = safeApiCall(gson) {
        val dto = FeedCommentRequestDTO.fromModel(request)
        api.writeComment(postID, dto)
    }.toModel()

    override suspend fun writeReply(commentID: String, request: FeedCreateComment): FeedComment = safeApiCall(gson) {
        val dto = FeedCommentRequestDTO.fromModel(request)
        api.writeReply(commentID, dto)
    }.toModel()

    override suspend fun deleteComment(commentID: String) = safeApiCall(gson) {
        val response = api.deleteComment(commentID)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun vote(commentID: String, type: FeedVoteType) = safeApiCall(gson) {
        api.vote(commentID, mapOf("vote" to type.name))
    }

    override suspend fun deleteVote(commentID: String) = safeApiCall(gson) {
        api.deleteVote(commentID)
    }

    override suspend fun reportComment(commentID: String, reason: FeedReportType, detail: String) = safeApiCall(gson) {
        api.reportComment(commentID, mapOf("reason" to reason.name, "detail" to detail))
    }
}