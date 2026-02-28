package org.sparcs.soap.App.Domain.Repositories.Ara

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.AraCommentApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.CommentPatchRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.CommentPostRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.CommentReportRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.ThreadedCommentPostRequest
import retrofit2.HttpException
import javax.inject.Inject

interface AraCommentRepositoryProtocol {

    suspend fun upVoteComment(commentID: Int)
    suspend fun downVoteComment(commentID: Int)
    suspend fun cancelVote(commentID: Int)
    suspend fun writeComment(postID: Int, content: String): AraPostComment
    suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment
    suspend fun deleteComment(commentID: Int)
    suspend fun editComment(commentID: Int, content: String): AraPostComment
    suspend fun reportComment(commentID: Int, type: AraContentReportType)
}

class AraCommentRepository @Inject constructor(
    private val araCommentApi: AraCommentApi,
    private val gson: Gson = Gson(),
) : AraCommentRepositoryProtocol {

    override suspend fun upVoteComment(commentID: Int) = safeApiCall(gson) {
        araCommentApi.upVoteComment(commentID)
    }

    override suspend fun downVoteComment(commentID: Int) = safeApiCall(gson) {
        araCommentApi.downVoteComment(commentID)
    }

    override suspend fun cancelVote(commentID: Int) = safeApiCall(gson) {
        araCommentApi.cancelVote(commentID)
    }

    override suspend fun writeComment(postID: Int, content: String): AraPostComment = safeApiCall(gson) {
        araCommentApi.writeComment(
            CommentPostRequest(parent_article = postID, content = content)
        )
    }.toModel()

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment = safeApiCall(gson) {
        araCommentApi.writeThreadedComment(
            ThreadedCommentPostRequest(parent_comment = commentID, content = content)
        )
    }.toModel()

    override suspend fun deleteComment(commentID: Int) = safeApiCall(gson) {
        val response = araCommentApi.deleteComment(commentID)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun editComment(commentID: Int, content: String): AraPostComment = safeApiCall(gson) {
        araCommentApi.editComment(
            commentID,
            CommentPatchRequest(content = content)
        )
    }.toModel()

    override suspend fun reportComment(commentID: Int, type: AraContentReportType) = safeApiCall(gson) {
        araCommentApi.reportComment(
            CommentReportRequest(
                parent_comment = commentID,
                type = "others",
                content = type.name
            )
        )
    }
}