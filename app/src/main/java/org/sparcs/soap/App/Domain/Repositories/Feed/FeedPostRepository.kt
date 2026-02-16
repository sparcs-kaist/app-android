package org.sparcs.soap.App.Domain.Repositories.Feed

import android.util.Log
import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Error.Feed.FeedDeletionError
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import org.sparcs.soap.App.Networking.RequestDTO.Feed.FeedPostRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedPostApi
import org.sparcs.soap.App.Shared.Mocks.mock
import retrofit2.HttpException
import javax.inject.Inject

interface FeedPostRepositoryProtocol {
    suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage
    suspend fun fetchPost(postID: String): FeedPost
    suspend fun writePost(request: FeedCreatePost)
    suspend fun deletePost(postID: String)
    suspend fun vote(postID: String, type: FeedVoteType)
    suspend fun deleteVote(postID: String)
    suspend fun reportPost(postID: String, reason: FeedReportType)
}

class FeedPostRepository @Inject constructor(
    private val api: FeedPostApi,
    private val gson: Gson = Gson(),
) : FeedPostRepositoryProtocol {

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage = try {
        api.fetchPosts(cursor, page).toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun fetchPost(postID: String): FeedPost = try {
        api.fetchPost(postID).toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writePost(request: FeedCreatePost) {
        try {
            val dto = FeedPostRequestDTO.fromModel(request)
            api.writePost(dto)
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deletePost(postID: String) {
        try {
            val response = api.deletePost(postID)
            if (response.isSuccessful) return

            if (response.code() == 409) {
                val body = response.errorBody()?.string() ?: ""
                val bodyLower = body.lowercase()
                val deletionError = when {
                    bodyLower.contains("comments") -> FeedDeletionError.PostHasComments
                    bodyLower.contains("vote") -> FeedDeletionError.PostHasVotes
                    else -> {
                        Log.e("FeedCommentRepository", body)
                        FeedDeletionError.Unknown
                    }
                }

                throw deletionError
            }

            throw HttpException(response)

        } catch (e: Exception) {
            if (e is FeedDeletionError) throw e
            handleApiError(gson, e)
        }
    }

    override suspend fun vote(postID: String, type: FeedVoteType) = try {
        api.vote(postID, mapOf("vote" to type.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun deleteVote(postID: String) = try {
        api.deleteVote(postID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType) = try {
        api.reportPost(postID, mapOf("reason" to reason.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}

class FakeFeedPostRepository: FeedPostRepositoryProtocol {
    override suspend fun fetchPost(postID: String): FeedPost {
       return FeedPost.mock()
    }
    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
      return FeedPostPage(
          items = listOf(),
          nextCursor = null,
          hasNext = false
      )
    }
    override suspend fun writePost(request: FeedCreatePost) {}
    override suspend fun deletePost(postID: String) {}
    override suspend fun vote(postID: String, type: FeedVoteType) {}
    override suspend fun deleteVote(postID: String) {}
    override suspend fun reportPost(postID: String, reason: FeedReportType) {}
}