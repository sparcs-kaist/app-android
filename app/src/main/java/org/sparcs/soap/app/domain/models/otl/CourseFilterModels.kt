package org.sparcs.soap.app.domain.models.otl

import androidx.annotation.StringRes
import org.sparcs.soap.R
const val ETC_DEPARTMENT_ID = "ETC"

data class CourseFilterOption(
    val id: String,
    val label: String
)

data class CourseFilterState(
    val classifications: List<String> = emptyList(),
    val departments: List<String> = emptyList(),
    val levels: List<String> = emptyList(),
    val period: String? = null
) {
    fun isEmpty(): Boolean =
        classifications.isEmpty() && departments.isEmpty() && levels.isEmpty() && period == null
}

enum class CourseFilterCategory(@get:StringRes val labelResId: Int) {
    Classification(R.string.filter_classification),
    Department(R.string.filter_department),
    Level(R.string.filter_level),
    Period(R.string.filter_period)
}
