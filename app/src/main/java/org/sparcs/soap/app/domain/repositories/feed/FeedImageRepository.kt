package org.sparcs.soap.app.domain.repositories.feed

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.app.domain.enums.feed.FeedPostPhotoItem
import org.sparcs.soap.app.domain.helpers.UploadImageDecoder
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedImageApi
import org.sparcs.soap.app.shared.extensions.compressForUpload
import javax.inject.Inject

interface FeedImageRepositoryProtocol {
    suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage
}

private const val FEED_IMAGE_MAX_MB = 10.0

class FeedImageRepository @Inject constructor(
    private val api: FeedImageApi,
    private val gson: Gson = Gson(),
) : FeedImageRepositoryProtocol {

    override suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage {
        val imageData = withContext(Dispatchers.Default) {
            item.image.compressForUpload(maxSizeMB = FEED_IMAGE_MAX_MB, maxDimension = UploadImageDecoder.MAX_DIMENSION)
        }

        val filePart = MultipartBody.Part.createFormData(
            "file",
            "image.jpg",
            imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        val descriptionPart = item.description.toRequestBody("text/plain".toMediaTypeOrNull())
        val spoilerPart = (if (item.spoiler) "true" else "false").toRequestBody("text/plain".toMediaTypeOrNull())

        return safeApiCall(gson) { api.uploadPostImage(filePart, descriptionPart, spoilerPart) }.toModel()
    }
}
