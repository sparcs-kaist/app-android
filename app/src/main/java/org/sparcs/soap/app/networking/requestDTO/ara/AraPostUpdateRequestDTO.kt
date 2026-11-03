package org.sparcs.soap.app.networking.requestDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraUpdatePost
import org.sparcs.soap.app.networking.responseDTO.ara.AraMarketMetadataDTO

data class AraPostUpdateRequestDTO(
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("attachments")
    val attachments: List<Int>,

    @SerializedName("is_content_sexual")
    val isNSFW: Boolean,

    @SerializedName("is_content_social")
    val isPolitical: Boolean,

    @SerializedName("metadata")
    val metadata: AraMarketMetadataDTO?
) {
    companion object {
        fun fromModel(model: AraUpdatePost) = AraPostUpdateRequestDTO(
            title = model.title,
            content = model.content,
            attachments = model.attachmentIds,
            isNSFW = model.isNSFW,
            isPolitical = model.isPolitical,
            metadata = model.marketListing?.let(AraMarketMetadataDTO::fromModel)
        )
    }
}
