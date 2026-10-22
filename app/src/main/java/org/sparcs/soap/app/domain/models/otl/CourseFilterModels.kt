package org.sparcs.soap.app.domain.models.otl

import androidx.annotation.StringRes
import org.sparcs.soap.R

data class CourseFilterOption(
    val id: String,
    val label: String
)

data class CourseFilterState(
    val classifications: List<String> = emptyList(),
    val departments: List<DepartmentOption> = emptyList(),
    val levels: List<String> = emptyList(),
    val period: String? = null
) {
    fun isEmpty(): Boolean =
        classifications.isEmpty() && departments.isEmpty() && levels.isEmpty() && period == null

    fun selectedOptionIds(category: CourseFilterCategory): List<String> = when (category) {
        CourseFilterCategory.Classification -> classifications
        CourseFilterCategory.Department -> departments.map { it.id.toString() }
        CourseFilterCategory.Level -> levels
        CourseFilterCategory.Period -> listOfNotNull(period)
    }

    fun withSelection(category: CourseFilterCategory, ids: List<String>): CourseFilterState = when (category) {
        CourseFilterCategory.Classification -> copy(classifications = ids)
        CourseFilterCategory.Department -> copy(departments = departments.filter { it.id.toString() in ids })
        CourseFilterCategory.Level -> copy(levels = ids)
        CourseFilterCategory.Period -> copy(period = ids.firstOrNull())
    }
}

enum class CourseFilterCategory(@get:StringRes val labelResId: Int) {
    Classification(R.string.filter_classification),
    Department(R.string.filter_department),
    Level(R.string.filter_level),
    Period(R.string.filter_period)
}
