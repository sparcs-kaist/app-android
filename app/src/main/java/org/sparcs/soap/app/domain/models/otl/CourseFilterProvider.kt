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
                CourseFilterOption("OE", stringResource(R.string.filter_other_elective)),
                CourseFilterOption("ETC", stringResource(R.string.filter_etc))
            )

            CourseFilterCategory.Department -> listOf(
                CourseFilterOption("9948", stringResource(R.string.dept_hss)),
                CourseFilterOption("709", stringResource(R.string.dept_ce)),
                CourseFilterOption("11481", stringResource(R.string.dept_btm)),
                CourseFilterOption("9942", stringResource(R.string.dept_me)),
                CourseFilterOption("623", stringResource(R.string.dept_ph)),
                CourseFilterOption("850", stringResource(R.string.dept_bis)),
                CourseFilterOption("1197", stringResource(R.string.dept_ie)),
                CourseFilterOption("625", stringResource(R.string.dept_id)),
                CourseFilterOption("733", stringResource(R.string.dept_bs)),
                CourseFilterOption("833", stringResource(R.string.dept_mas)),
                CourseFilterOption("639", stringResource(R.string.dept_nqe)),
                CourseFilterOption("9947", stringResource(R.string.dept_ee)),
                CourseFilterOption("9945", stringResource(R.string.dept_cs)),
                CourseFilterOption("9944", stringResource(R.string.dept_ae)),
                CourseFilterOption("620", stringResource(R.string.dept_ch)),
                CourseFilterOption("701", stringResource(R.string.dept_cbe)),
                CourseFilterOption("732", stringResource(R.string.dept_ms)),
                CourseFilterOption("15784", stringResource(R.string.dept_ts)),
                CourseFilterOption("20184", stringResource(R.string.dept_ss)),
                CourseFilterOption("20684", stringResource(R.string.dept_bcs)),
                CourseFilterOption(ETC_DEPARTMENT_ID, stringResource(R.string.dept_etc))
            )

            CourseFilterCategory.Level -> (1..9).map {
                CourseFilterOption("${it}00", stringResource(R.string.filter_year_format, "${it}00"))
            }

            CourseFilterCategory.Period -> listOf(
                CourseFilterOption("0", stringResource(R.string.filter_this_semester)),
                CourseFilterOption("1", stringResource(R.string.filter_within_1year)),
                CourseFilterOption("3", stringResource(R.string.filter_within_3years))
            )
        }
    }
}
