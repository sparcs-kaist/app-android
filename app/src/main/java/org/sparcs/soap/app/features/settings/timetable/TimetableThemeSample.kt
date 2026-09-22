package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureClass
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.shared.mocks.otl.mock

internal object TimetableThemeSample {

    fun create(
        calculus: String,
        physics: String,
        design: String,
        programming: String,
        chemistry: String,
        lab: String,
        club: String,
    ): List<LectureItem> =
        buildList {
            addAll(course(0, calculus, listOf(DayType.MON, DayType.WED), 540, 630, "101"))
            addAll(course(1, physics, listOf(DayType.TUE, DayType.THU), 660, 780, "102"))
            addAll(course(2, programming, listOf(DayType.MON, DayType.WED), 780, 870, "103"))
            addAll(course(3, design, listOf(DayType.TUE), 810, 900, "104"))
            addAll(course(4, chemistry, listOf(DayType.FRI), 570, 660, "105"))
            addAll(course(5, lab, listOf(DayType.THU), 810, 900, "106"))
            addAll(course(6, club, listOf(DayType.FRI), 780, 870, "", "N1"))
        }

    private fun course(
        id: Int,
        title: String,
        days: List<DayType>,
        begin: Int,
        end: Int,
        room: String,
        building: String = "E11",
    ): List<LectureItem> {
        val classes = days.map { LectureClass(it, begin, end, building, "", room) }
        val lecture = Lecture.mock().copy(
            id = id, courseID = id, name = title, subtitle = "", classes = classes
        )
        return classes.mapIndexed { index, lectureClass ->
            LectureItem(lecture = lecture, lectureClass = lectureClass, index = index)
        }
    }
}

@Composable
internal fun rememberThemeSample(): List<LectureItem> {
    val calculus = stringResource(R.string.theme_sample_3)
    val physics = stringResource(R.string.theme_sample_2)
    val design = stringResource(R.string.theme_sample_1)
    val programming = stringResource(R.string.theme_sample_programming)
    val chemistry = stringResource(R.string.theme_sample_chemistry)
    val lab = stringResource(R.string.theme_sample_lab)
    val club = stringResource(R.string.theme_sample_club)
    return remember(calculus, physics, design, programming, chemistry, lab, club) {
        TimetableThemeSample.create(calculus, physics, design, programming, chemistry, lab, club)
    }
}
