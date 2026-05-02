package org.sparcs.soap.App.Domain.Repositories.Ara

import android.graphics.Bitmap
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Enums.Ara.PostListType
import org.sparcs.soap.App.Domain.Enums.Ara.PostOrigin
import org.sparcs.soap.App.Domain.Models.Ara.AraAttachment
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraCreatePost
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostPage
import org.sparcs.soap.App.Networking.RequestDTO.Ara.AraPostRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Ara.AraPortalNoticeDTO
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.AraBoardApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Ara.PostReportRequest
import org.sparcs.soap.App.Shared.Extensions.compressForUpload
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
    suspend fun addBookmark(postID: Int): Int
    suspend fun removeBookmark(bookmarkID: Int)

    suspend fun fetchPortalNotices(
        boardId: Int? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): List<AraPortalNoticeDTO>

    suspend fun fetchTrendingPortalNotices(): List<AraPortalNoticeDTO>
}


class AraBoardRepository @Inject constructor(
    private val api: AraBoardApi,
    private val gson: Gson = Gson(),
) : AraBoardRepositoryProtocol {

    // MARK: - Caches
    private var cachedBoards: List<AraBoard>? = null
    private val mutex = Mutex()

    override suspend fun fetchBoards(): List<AraBoard> {
        val cached = mutex.withLock { cachedBoards }
        if (cached != null) return cached

        val boards = safeApiCall(gson) {
            api.fetchBoards()
        }.map { it.toModel() }

        mutex.withLock { this.cachedBoards = boards }
        return boards
    }

    override suspend fun fetchPosts(
        type: PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String?,
    ): AraPostPage = safeApiCall(gson) {
        when (type) {
            is PostListType.Board -> api.fetchPosts(page, pageSize, parentBoard = type.boardID, searchKeyword = searchKeyword)
            is PostListType.User -> api.fetchPosts(page, pageSize, createdBy = type.userID, searchKeyword = searchKeyword)
            is PostListType.All -> api.fetchPosts(page, pageSize, searchKeyword = searchKeyword)
        }
    }.toModel()

    override suspend fun fetchPost(origin: PostOrigin?, postID: Int): AraPost = safeApiCall(gson) {
        api.fetchPost(postID, topicId = (origin as? PostOrigin.Topic)?.topicID)
    }.toModel()

    override suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage = safeApiCall(gson) {
        api.fetchBookmarks(page = page, pageSize = pageSize)
    }.toModel()

    override suspend fun uploadImage(image: Bitmap): AraAttachment = safeApiCall(gson) {
        val compressed = image.compressForUpload(maxSizeMB = 1.0, maxDimension = 500)
            ?: throw IllegalArgumentException("Failed to compress image")
        val part = MultipartBody.Part.createFormData("file", "image.jpg", compressed.toRequestBody())
        api.uploadImage(part)
    }.toModel()

    override suspend fun writePost(request: AraCreatePost) = safeApiCall(gson) {
        api.writePost(AraPostRequestDTO.fromModel(request))
    }

    override suspend fun upVotePost(postID: Int) = safeApiCall(gson) {
        api.upVote(postID)
    }

    override suspend fun downVotePost(postID: Int) = safeApiCall(gson) {
        api.downVote(postID)
    }

    override suspend fun cancelVote(postID: Int) = safeApiCall(gson) {
        api.cancelVote(postID)
    }

    override suspend fun reportPost(postID: Int, type: AraContentReportType) = safeApiCall(gson) {
        api.report(
            PostReportRequest(
                post_id = postID,
                type = "others",
                content = type.name
            )
        )
    }

    override suspend fun deletePost(postID: Int) = safeApiCall(gson) {
        val response = api.delete(postID)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    override suspend fun addBookmark(postID: Int): Int = safeApiCall(gson) {
        api.addBookmark(mapOf("parent_article" to postID))
    }.id

    override suspend fun removeBookmark(bookmarkID: Int) = safeApiCall(gson) {
        val response = api.removeBookmark(bookmarkID)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    override suspend fun fetchPortalNotices(
        boardId: Int?,
        page: Int,
        pageSize: Int
    ): List<AraPortalNoticeDTO> = safeApiCall(gson) {
        api.fetchPortalNotices(boardId, page, pageSize)
    }

    override suspend fun fetchTrendingPortalNotices(): List<AraPortalNoticeDTO> = safeApiCall(gson) {
        api.fetchTrendingPortalNotices()
    }
}
