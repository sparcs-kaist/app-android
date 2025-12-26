package org.sparcs.Networking.RequestDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Models.Ara.AraCreatePost
import org.sparcs.Shared.Extensions.toHTMLParagraphs

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
    val board: Int
){
    companion object {
        fun fromModel(model: AraCreatePost): AraPostRequestDTO {
            val attachmentsInHTML = model.attachments.mapNotNull { attachment ->
                val fileURL = attachment.file
                if (fileURL != null) {
                    """
                    <p><img src="$fileURL" width="500" data-attachment="${attachment.id}"></p>
                    """.trimIndent()
                } else {
                    null
                }
            }.joinToString("")

            return AraPostRequestDTO(
                title = model.title,
                content = model.content.toHTMLParagraphs() + attachmentsInHTML,
                attachments = model.attachments.map { it.id },
                topic = model.topic?.id,
                isNSFW = model.isNSFW,
                isPolitical = model.isPolitical,
                nicknameType = model.nicknameType.name,
                board = model.board.id
            )
        }
    }
}