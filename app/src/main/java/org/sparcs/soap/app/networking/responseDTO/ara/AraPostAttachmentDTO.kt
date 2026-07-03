package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
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

    @SerializedName("alias")
    val alias: String?,

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
            } catch (_: DateTimeParseException) {
                Date()
            },
            file = URL(file),
            filename = alias?.takeIf { it.isNotBlank() } ?: try {
                URL(file).path.substringAfterLast('/').takeIf { it.isNotBlank() }
                    ?: "Untitled"
            } catch (_: Exception) {
                "Untitled"
            },
            size = size,
            mimeType = mimeType
        )
    }

}