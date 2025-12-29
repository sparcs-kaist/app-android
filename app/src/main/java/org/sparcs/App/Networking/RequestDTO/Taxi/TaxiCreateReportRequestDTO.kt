package org.sparcs.App.Networking.RequestDTO.Taxi

import org.sparcs.App.Domain.Models.Taxi.TaxiCreateReport
import org.sparcs.App.Shared.Extensions.toISO8601

// DTO
data class TaxiCreateReportRequestDTO(
    val reportedId: String,
    val type: String,
    val etcDetail: String?,
    val time: String,
    val roomId: String
){
    companion object{
        fun fromModel(model: TaxiCreateReport): TaxiCreateReportRequestDTO {
            return TaxiCreateReportRequestDTO(
                reportedId = model.reportedID,
                type = model.reason.value,
                etcDetail = model.etcDetails,
                time = model.time.toISO8601(),
                roomId = model.roomID
            )
        }
    }
}
