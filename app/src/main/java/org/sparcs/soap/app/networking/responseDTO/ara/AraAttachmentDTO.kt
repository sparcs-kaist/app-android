package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraAttachment
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date

data class AraAttachmentDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("file")
    val file: String,

    @SerializedName("size")
    val size: Int,

    @SerializedName("mimetype")
    val mimeType: String,

    @SerializedName("created_at")
    val createdAt: String
) {
    fun toModel(): AraAttachment {
        return AraAttachment(
            id = id,
            file = URL(file),
            size = size,
            mimeType = mimeType,
            createdAt = try {
                Date.from(Instant.parse(createdAt))
            } catch (_: DateTimeParseException) {
                Date()
            }
        )
    }

}