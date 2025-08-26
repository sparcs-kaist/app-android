package com.example.soap.Networking.ResponseDTO.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiNotice
import com.example.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName
import java.net.URL

data class TaxiNoticeDTO(
    val notices: List<NoticeElement>
) {
    data class NoticeElement(
        @SerializedName("_id")
        val id: String,

        @SerializedName("title")
        val title: String,

        @SerializedName("notion_url")
        val notionURL: String,

        @SerializedName("is_pinned")
        val isPinned: Boolean,

        @SerializedName("is_active")
        val isActive: Boolean,

        @SerializedName("createdAt")
        val createdAt: String,

        @SerializedName("updatedAt")
        val updatedAt: String
    ) {
        fun toModel(): TaxiNotice {
            val url = try {
                URL(notionURL)
            } catch (e: Exception) {
                throw TaxiNoticeConversionException.InvalidURL
            }

            val createdDate = createdAt.toDate()
                ?: throw TaxiNoticeConversionException.InvalidCreatedAt

            val updatedDate = updatedAt.toDate()
                ?: throw TaxiNoticeConversionException.InvalidUpdatedAt

            return TaxiNotice(
                id = id,
                title = title,
                notionURL = url,
                isPinned = isPinned,
                isActive = isActive,
                createdAt = createdDate,
                updatedAt = updatedDate
            )
        }
    }
}

sealed class TaxiNoticeConversionException : Exception() {
    data object InvalidURL : TaxiNoticeConversionException() {
        private fun readResolve(): Any = InvalidURL
    }

    data object InvalidCreatedAt : TaxiNoticeConversionException() {
        private fun readResolve(): Any = InvalidCreatedAt
    }

    data object InvalidUpdatedAt : TaxiNoticeConversionException() {
        private fun readResolve(): Any = InvalidUpdatedAt
    }
}
