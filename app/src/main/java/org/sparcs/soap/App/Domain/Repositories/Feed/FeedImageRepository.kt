package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.App.Domain.Enums.Feed.FeedPostPhotoItem
import org.sparcs.soap.App.Domain.Models.Feed.FeedImage
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedImageApi
import org.sparcs.soap.App.Shared.Extensions.compressForUpload
import javax.inject.Inject

interface FeedImageRepositoryProtocol {
    suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage
}

class FeedImageRepository @Inject constructor(
    private val api: FeedImageApi,
    private val gson: Gson = Gson(),
) : FeedImageRepositoryProtocol {

    override suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage = safeApiCall(gson) {
        val imageData = item.image.compressForUpload(10.0)
            ?: throw IllegalArgumentException("Image compression failed")

        val filePart = MultipartBody.Part.createFormData(
            "file",
            "image.jpg",
            imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        val descriptionPart = item.description.toRequestBody("text/plain".toMediaTypeOrNull())
        val spoilerPart = (if (item.spoiler) "true" else "false").toRequestBody("text/plain".toMediaTypeOrNull())

        api.uploadPostImage(filePart, descriptionPart, spoilerPart)
    }.toModel()
}
