package com.sparcs.soap.Domain.Repositories.Ara

import android.graphics.Bitmap
import com.google.gson.Gson
import com.sparcs.soap.Domain.Enums.Ara.AraContentReportType
import com.sparcs.soap.Domain.Enums.Ara.PostListType
import com.sparcs.soap.Domain.Enums.Ara.PostOrigin
import com.sparcs.soap.Domain.Models.Ara.AraAttachment
import com.sparcs.soap.Domain.Models.Ara.AraBoard
import com.sparcs.soap.Domain.Models.Ara.AraCreatePost
import com.sparcs.soap.Domain.Models.Ara.AraPost
import com.sparcs.soap.Domain.Models.Ara.AraPostPage
import com.sparcs.soap.Networking.RequestDTO.Ara.AraPostRequestDTO
import com.sparcs.soap.Networking.ResponseDTO.handleApiError
import com.sparcs.soap.Networking.ResponseDTO.parseReportCommentError
import com.sparcs.soap.Networking.RetrofitAPI.Ara.AraBoardApi
import com.sparcs.soap.Shared.Extensions.compressForUpload
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


interface AraBoardRepositoryProtocol {
    suspend fun fetchBoards(): List<AraBoard>
    suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String? = null,
    ): AraPostPage

    suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost
    suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage
    suspend fun uploadImage(image: Bitmap): AraAttachment
    suspend fun writePost(request: AraCreatePost)
    suspend fun upVotePost(postID: Int)
    suspend fun downVotePost(postID: Int)
    suspend fun cancelVote(postID: Int)
    suspend fun reportPost(postID: Int, type: AraContentReportType)
    suspend fun deletePost(postID: Int)
    suspend fun addBookmark(postID: Int)
    suspend fun removeBookmark(bookmarkID: Int)
}


class AraBoardRepository @Inject constructor(
    private val api: AraBoardApi,
    private val gson: Gson = Gson(),
) : AraBoardRepositoryProtocol {

    // MARK: - Caches
    private var cachedBoards: List<AraBoard>? = null
    private val mutex = Mutex()

    override suspend fun fetchBoards(): List<AraBoard> = mutex.withLock {
        cachedBoards?.let { return it }

        val response = api.fetchBoards()
        val boards = response.map { it.toModel() }
        cachedBoards = boards
        return boards
    }

    override suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?,
    ): AraPostPage {
        try {
            val response = when (type) {
                is PostListType.Board -> api.fetchPosts(
                    page, pageSize, parentBoard = type.boardID, searchKeyword = searchKeyword
                )

                is PostListType.User -> api.fetchPosts(
                    page, pageSize, createdBy = type.userID, searchKeyword = searchKeyword
                )

                is PostListType.All -> api.fetchPosts(
                    page, pageSize, searchKeyword = searchKeyword
                )
            }
            return response.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost {
        try {
            val response = api.fetchPost(
                postID,
                topicId = (origin as? PostOrigin.Topic)?.topicID
            )
            return response.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage {
        try {
            val response = api.fetchBookmarks(page = page, pageSize = pageSize)
            return response.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun uploadImage(image: Bitmap): AraAttachment {
        try {
            val compressed = image.compressForUpload(maxSizeMB = 1.0, maxDimension = 500)
                ?: throw IllegalArgumentException("Failed to compress image")
            val part = MultipartBody.Part.createFormData(
                "file", "image.jpg", compressed.toRequestBody()
            )
            val response = api.uploadImage(part)
            return response.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun writePost(request: AraCreatePost) {
        try {
            api.writePost(AraPostRequestDTO.fromModel(request))
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun upVotePost(postID: Int) = try {
        api.upVote(postID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun downVotePost(postID: Int) = try {
        api.downVote(postID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun cancelVote(postID: Int) = try {
        api.cancelVote(postID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun reportPost(postID: Int, type: AraContentReportType) =
        try {
            api.report(
                PostReportRequest(
                    post_id = postID,
                    type = "others",
                    content = type.name
                )
            )
        } catch (e: Exception) {
            throw parseReportCommentError(e)
        }

    override suspend fun deletePost(postID: Int) {
        try {
            val response = api.delete(postID)
            if (!response.isSuccessful) {
                throw Exception("Delete failed: ${response.code()}")
            }
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }


    override suspend fun addBookmark(postID: Int) = try {
        api.addBookmark(mapOf("parent_article" to postID))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun removeBookmark(bookmarkID: Int) = try {
        api.removeBookmark(bookmarkID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}

data class PostReportRequest(
    val post_id: Int,
    val type: String,
    val content: String,
)