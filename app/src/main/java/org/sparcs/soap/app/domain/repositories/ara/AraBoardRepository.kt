package org.sparcs.soap.app.domain.repositories.ara

import android.graphics.Bitmap
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.enums.ara.PostOrigin
import org.sparcs.soap.app.domain.models.ara.AraAttachment
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraCreatePost
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostPage
import org.sparcs.soap.app.networking.requestDTO.ara.AraPostRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraBoardApi
import org.sparcs.soap.app.networking.retrofitAPI.ara.PostReportRequest
import org.sparcs.soap.app.shared.extensions.compressForUpload
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
            .filter { !it.slug.contains("internal") } // FIXME: temporary workaround for filtering internal board

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
            is PostListType.Board -> api.fetchPosts(
                page,
                pageSize,
                parentBoard = type.boardID,
                searchKeyword = searchKeyword
            )

            is PostListType.User -> api.fetchPosts(
                page,
                pageSize,
                createdBy = type.userID,
                searchKeyword = searchKeyword
            )

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
        val part =
            MultipartBody.Part.createFormData("file", "image.jpg", compressed.toRequestBody())
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
                postId = postID,
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
}
