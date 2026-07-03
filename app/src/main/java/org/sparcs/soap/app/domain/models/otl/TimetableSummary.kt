package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType

data class TimetableSummary(
    val id: Int,
    val title: String,
    val year: Int,
    val semester: SemesterType
){
    companion object
}