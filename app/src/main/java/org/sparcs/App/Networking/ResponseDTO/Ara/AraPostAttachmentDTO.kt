package org.sparcs.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Ara.AraPostAttachment
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date

data class AraPostAttachmentDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("file")
    val file: String,

    @SerializedName("size")
    val size: Int,

    @SerializedName("mimetype")
    val mimeType: String

) {
    fun toModel(): AraPostAttachment {
        return AraPostAttachment(
            id = id,
            createdAt = try {
                Date.from(Instant.parse(createdAt))
            } catch (e: DateTimeParseException) {
                Date()
            },
            file = URL(file),
            filename = try {
                URL(file).path.substringAfterLast('/')
            } catch (e: Exception) {
                "Untitled"
            },
            size = size,
            mimeType = mimeType
        )
    }

}