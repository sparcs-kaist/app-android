package org.sparcs.soap.app.networking.requestDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraCreatePost
import org.sparcs.soap.app.networking.responseDTO.ara.AraMarketMetadataDTO

data class AraPostRequestDTO(
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("attachments")
    val attachments: List<Int>,

    @SerializedName("parent_topic")
    val topic: Int?,

    @SerializedName("is_content_sexual")
    val isNSFW: Boolean,

    @SerializedName("is_content_social")
    val isPolitical: Boolean,

    @SerializedName("name_type")
    val nicknameType: String,

    @SerializedName("parent_board")
    val board: Int,

    @SerializedName("metadata")
    val metadata: AraMarketMetadataDTO?
){
    companion object {
        fun fromModel(model: AraCreatePost) = AraPostRequestDTO(
            title = model.title,
            content = model.content,
            attachments = model.attachmentIds,
            topic = model.topic?.id,
            isNSFW = model.isNSFW,
            isPolitical = model.isPolitical,
            nicknameType = model.nicknameType.type,
            board = model.board.id,
            metadata = model.marketListing?.let(AraMarketMetadataDTO::fromModel)
        )
    }
}
