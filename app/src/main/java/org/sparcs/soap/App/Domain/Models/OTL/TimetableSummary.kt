package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType

data class TimetableSummary(
    val id: Int,
    val title: String,
    val year: Int,
    val semester: SemesterType
){
    companion object
}