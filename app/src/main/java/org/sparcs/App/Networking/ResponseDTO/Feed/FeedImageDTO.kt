package org.sparcs.App.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Feed.FeedImage

data class FeedImageDTO (
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("mime_type")
    val mimeType: String,

    @SerializedName("size")
    val size: Int,

    @SerializedName("spoiler")
    val spoiler: Boolean?
) {
    fun toModel(): FeedImage {
        return FeedImage(
            id = id,
            url = url,
            mimeType = mimeType,
            size = size,
            spoiler = spoiler
        )
    }
}
