package org.sparcs.soap.app.networking.responseDTO.feed

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.feed.FeedImage

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
