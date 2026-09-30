package org.sparcs.soap.buddyPreviewSupport.otl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewModelProtocol
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewState
import org.sparcs.soap.app.features.timetable.creditCalculation.creditPreviewState

internal class PreviewCreditCalculationViewModel(
    initialState: CreditCalculationViewState = creditPreviewState(),
) : CreditCalculationViewModelProtocol {
    private val mutableState = MutableStateFlow(initialState)
    override val state = mutableState.asStateFlow()
    override val alertState: AlertState? = null
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun load(forceRefresh: Boolean) {
        mutableState.value = creditPreviewState()
    }
    override fun refresh() = load(forceRefresh = true)
    override fun setGrade(grade: LectureGrade?, lectureID: Int) {
        mutableState.update { it.copy(grades = if (grade == null) it.grades - lectureID else it.grades + (lectureID to grade)) }
    }
    override fun updateRequirements(requirements: CreditRequirements) {
        mutableState.update { it.copy(requirements = requirements) }
    }
}
