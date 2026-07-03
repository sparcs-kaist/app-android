package org.sparcs.soap.app.networking.requestDTO.taxi

import org.sparcs.soap.app.domain.models.taxi.TaxiCreateReport
import org.sparcs.soap.app.shared.extensions.toISO8601

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
