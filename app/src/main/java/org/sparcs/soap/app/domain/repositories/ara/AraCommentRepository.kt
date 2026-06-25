package org.sparcs.soap.app.domain.repositories.ara

import com.google.gson.Gson
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraCommentApi
import org.sparcs.soap.app.networking.retrofitAPI.ara.CommentPatchRequest
import org.sparcs.soap.app.networking.retrofitAPI.ara.CommentPostRequest
import org.sparcs.soap.app.networking.retrofitAPI.ara.CommentReportRequest
import org.sparcs.soap.app.networking.retrofitAPI.ara.ThreadedCommentPostRequest
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
            CommentPostRequest(parentArticle = postID, content = content)
        )
    }.toModel()

    override suspend fun writeThreadedComment(commentID: Int, content: String): AraPostComment = safeApiCall(gson) {
        araCommentApi.writeThreadedComment(
            ThreadedCommentPostRequest(parentComment = commentID, content = content)
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
                parentComment = commentID,
                type = "others",
                content = type.name
            )
        )
    }
}