package org.sparcs.soap.app.domain.models.otl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R

object CourseFilterProvider {
    @Composable
    fun getOptions(category: CourseFilterCategory): List<CourseFilterOption> {
        return when (category) {
            CourseFilterCategory.Classification -> listOf(
                CourseFilterOption("BR", stringResource(R.string.filter_basic_required)),
                CourseFilterOption("BE", stringResource(R.string.filter_basic_elective)),
                CourseFilterOption("MR", stringResource(R.string.filter_major_required)),
                CourseFilterOption("ME", stringResource(R.string.filter_major_elective)),
                CourseFilterOption("MGC", stringResource(R.string.filter_general_required)),
                CourseFilterOption("HSE", stringResource(R.string.filter_humanities)),
                CourseFilterOption("GR", stringResource(R.string.filter_common)),
                CourseFilterOption("EG", stringResource(R.string.filter_graduate)),
                CourseFilterOption("OE", stringResource(R.string.filter_other_elective))
            )

            CourseFilterCategory.Department -> emptyList()

            CourseFilterCategory.Level -> (1..9).map {
                CourseFilterOption("${it}00", stringResource(R.string.filter_year_format, "${it}00"))
            }

            CourseFilterCategory.Period -> listOf(
                CourseFilterOption("0", stringResource(R.string.filter_this_semester)),
                CourseFilterOption("1", stringResource(R.string.filter_within_1year)),
                CourseFilterOption("2", stringResource(R.string.filter_within_2years)),
                CourseFilterOption("3", stringResource(R.string.filter_within_3years))
            )
        }
    }
}
