package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.Widgets.AraPortalWidget.WidgetNoticeEntry

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
    fun toWidgetEntry(boardName: String): WidgetNoticeEntry {
        return WidgetNoticeEntry(
            id = araID,
            title = title,
            author = author,
            boardName = boardName,
            displayBoardName = boardName,
            boardSlug = "portal",
            iconResId = org.sparcs.soap.R.drawable.ic_widget_notice
        )
    }
}
