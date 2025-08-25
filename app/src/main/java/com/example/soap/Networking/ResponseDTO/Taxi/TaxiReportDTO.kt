package com.example.soap.Networking.ResponseDTO.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName

data class TaxiReportDTO(
    val reporting: List<TaxiReportDetail>,
    val reported: List<TaxiReportDetail>
) {
    data class ReportedID(

        @SerializedName("nickname")
        val nickname: String
    )

    data class TaxiReportDetail(
        @SerializedName("_id")
        val id: String,

        @SerializedName("reportedId")
        val reportedId: ReportedID,

        @SerializedName("type")
        val type: String,

        @SerializedName("etcDetail")
        val etcDetail: String,

        @SerializedName("time")
        val createdAt: String

    ) {
        fun toModel(reportType: TaxiReport.ReportType): TaxiReport {
            val reason = try {
                TaxiReport.ReportReason.from(type)
                    ?: throw TaxiReportConversionException.InvalidReason
            } catch (e: Exception) {
                throw TaxiReportConversionException.InvalidReason
            }

            val reportedDate = createdAt.toDate()
                ?: throw TaxiReportConversionException.InvalidDate

            return TaxiReport(
                id = id,
                nickname = reportedId.nickname,
                reportType = reportType,
                reason = reason,
                etcDetail = etcDetail,
                reportedAt = reportedDate
            )
        }
    }
}

sealed class TaxiReportConversionException : Exception() {
    data object InvalidReason : TaxiReportConversionException() {
        private fun readResolve(): Any = InvalidReason
    }

    data object InvalidDate : TaxiReportConversionException() {
        private fun readResolve(): Any = InvalidDate
    }
}
