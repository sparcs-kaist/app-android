package com.example.soap.Domain.Repositories.Feed

import com.example.soap.Domain.Models.Feed.FeedImage
import com.example.soap.Features.FeedPostCompose.FeedPostPhotoItem
import com.example.soap.Networking.RetrofitAPI.Feed.FeedImageApi
import com.example.soap.Shared.Extensions.compressForUpload
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

interface FeedImageRepositoryProtocol {
    suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage
}

class FeedImageRepository @Inject constructor(
    private val api: FeedImageApi,
) : FeedImageRepositoryProtocol {

    override suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage {
        val imageData = item.image.compressForUpload(10.0)
            ?: throw IllegalArgumentException("Image compression failed")

        val filePart = MultipartBody.Part.createFormData(
            "file",
            "image.jpg",
            imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        val descriptionPart = item.description.toRequestBody("text/plain".toMediaTypeOrNull())
        val spoilerPart =
            (if (item.spoiler) "true" else "false").toRequestBody("text/plain".toMediaTypeOrNull())

        val imageDTO = api.uploadPostImage(filePart, descriptionPart, spoilerPart)
        return imageDTO.toModel() ?: throw IllegalStateException("DTO to Model conversion failed")
    }
}
