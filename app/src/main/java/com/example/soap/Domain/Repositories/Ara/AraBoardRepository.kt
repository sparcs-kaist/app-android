package com.example.soap.Domain.Repositories.Ara

import android.graphics.Bitmap
import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraAttachment
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraCreatePost
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostPage
import com.example.soap.Networking.RequestDTO.Ara.AraPostRequestDTO
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardApi
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardTarget
import com.example.soap.Shared.Extensions.compressForUpload
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject


interface AraBoardRepositoryProtocol {
    suspend fun fetchBoards(): List<AraBoard>
    suspend fun fetchPosts(
        type: AraBoardTarget.PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String? = null,
    ): AraPostPage

    suspend fun fetchPost(origin: AraBoardTarget.PostOrigin?, postID: Int): AraPost
    suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage
    suspend fun uploadImage(image: Bitmap): AraAttachment
    suspend fun writePost(request: AraCreatePost)
    suspend fun upVotePost(postID: Int)
    suspend fun downVotePost(postID: Int)
    suspend fun cancelVote(postID: Int)
    suspend fun reportPost(postID: Int, type: AraContentReportType)
    suspend fun deletePost(postID: Int): Response<Unit>
    suspend fun addBookmark(postID: Int): Response<Unit>
    suspend fun removeBookmark(bookmarkID: Int): Response<Unit>
}


class AraBoardRepository @Inject constructor(
    private val api: AraBoardApi,
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
        type: AraBoardTarget.PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?,
    ): AraPostPage {
        val response = when (type) {
            is AraBoardTarget.PostListType.Board -> api.fetchPosts(
                page, pageSize, parentBoard = type.boardID
            )

            is AraBoardTarget.PostListType.User -> api.fetchPosts(
                page, pageSize, createdBy = type.userID
            )
        }
        return response.toModel()
    }

    override suspend fun fetchPost(origin: AraBoardTarget.PostOrigin?, postID: Int): AraPost {
        val response = api.fetchPost(
            postID,
            topicId = (origin as? AraBoardTarget.PostOrigin.Topic)?.topicID
        )
        return response.toModel()
    }

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage {
        val response = api.fetchBookmarks(page = page, pageSize = pageSize)
        return response.toModel()
    }

    override suspend fun uploadImage(image: Bitmap): AraAttachment {
        val compressed = image.compressForUpload(maxSizeMB = 1.0, maxDimension = 500)
            ?: throw IllegalArgumentException("Failed to compress image")
        val part = MultipartBody.Part.createFormData(
            "file", "image.jpg", compressed.toRequestBody()
        )
        val response = api.uploadImage(part)
        return response.toModel()
    }

    override suspend fun writePost(request: AraCreatePost) {
        api.writePost(AraPostRequestDTO.fromModel(request))
    }

    override suspend fun upVotePost(postID: Int) = api.upVote(postID)
    override suspend fun downVotePost(postID: Int) = api.downVote(postID)
    override suspend fun cancelVote(postID: Int) = api.cancelVote(postID)
    override suspend fun reportPost(postID: Int, type: AraContentReportType) =
        api.report(mapOf("post_id" to postID, "type" to type.name))

    override suspend fun deletePost(postID: Int): Response<Unit> {
        val response = api.delete(postID)
        if (!response.isSuccessful) throw HttpException(response)
        return response
    }

    override suspend fun addBookmark(postID: Int): Response<Unit> {
        val response = api.addBookmark(mapOf("parent_article" to postID))
        if (!response.isSuccessful) throw HttpException(response)
        return response
    }

    override suspend fun removeBookmark(bookmarkID: Int): Response<Unit> {
        val response = api.removeBookmark(bookmarkID)
        if (!response.isSuccessful) throw HttpException(response)
        return response
    }
}
