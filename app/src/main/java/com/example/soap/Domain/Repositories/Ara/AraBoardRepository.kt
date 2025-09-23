package com.example.soap.Domain.Repositories.Ara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Domain.Models.Ara.AraAttachment
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraCreatePost
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostPage
import com.example.soap.Networking.RequestDTO.AraPostRequestDTO
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardApi
import com.example.soap.Networking.RetrofitAPI.Ara.AraBoardTarget
import com.example.soap.Shared.Extensions.compressForUpload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


interface AraBoardRepositoryProtocol {
    suspend fun fetchBoards(): List<AraBoard>
    suspend fun fetchPosts(
        type: AraBoardTarget.PostListType,
        page: Int,
        pageSize: Int,
        searchKeyword: String? = null
    ): AraPostPage

    suspend fun fetchPost(origin: AraBoardTarget.PostOrigin?, postID: Int): AraPost
    suspend fun fetchBookmarks(page: Int, pageSize: Int): AraPostPage
    suspend fun uploadImage(image: Bitmap): AraAttachment
    suspend fun writePost(request: AraCreatePost)
    suspend fun upVotePost(postID: Int)
    suspend fun downVotePost(postID: Int)
    suspend fun cancelVote(postID: Int)
    suspend fun reportPost(postID: Int, type: AraContentReportType)
    suspend fun deletePost(postID: Int)
}

class AraBoardRepository @Inject constructor(
    private val api: AraBoardApi,
    @ApplicationContext private val context: Context
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
        searchKeyword: String?
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
        val response = api.fetchPosts(
            page = page,
            pageSize = pageSize,
        )
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

    override suspend fun deletePost(postID: Int) = api.delete(postID)


    suspend fun loadBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
            null
        }
    }

}
