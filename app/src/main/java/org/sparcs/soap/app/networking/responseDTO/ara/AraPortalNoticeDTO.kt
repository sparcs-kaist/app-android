package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPortalNotice

data class AraPortalNoticeDTO(
    @SerializedName("title")
    val title: String,

    @SerializedName("ara_article")
    val araID: Int,

    @SerializedName("writer_department")
    val author: String,

    @SerializedName("registered_at")
    val date: String,
) {
    fun toDomain(): AraPortalNotice = AraPortalNotice(
        id = araID,
        title = title,
        author = author,
        date = date,
    )
}
