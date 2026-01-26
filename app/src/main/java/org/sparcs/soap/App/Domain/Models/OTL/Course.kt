package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Helpers.CourseRepresentable
import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class Course(
    val id: Int,
    val code: String,
    val department: Department,
    val type: LocalizedString,
    val title: LocalizedString,
    val summary: String,
    val reviewTotalWeight: Double,
    override val grade: Double,
    override val load: Double,
    override val speech: Double,
    override val credit: Int,
    override val creditAu: Int,
    val numClasses: Int,
    val numLabs: Int
): CourseRepresentable {
    companion object{}
}