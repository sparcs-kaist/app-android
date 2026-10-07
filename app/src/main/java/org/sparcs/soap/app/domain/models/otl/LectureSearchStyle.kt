package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.R

enum class LectureSearchStyle(val rawValue: String, val titleRes: Int) {
    Fixed("sheet", R.string.lecture_search_fixed),
    Flexible("fullScreen", R.string.lecture_search_flexible),
}
