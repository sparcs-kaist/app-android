package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.models.otl.CreditBreakdown
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.domain.models.otl.Department
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.RetakeResolver
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.SemesterGradeSummary
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.toAlertState
import timber.log.Timber
import javax.inject.Inject

data class CreditCalculationViewState(
    val isLoading: Boolean = true,
    val error: Exception? = null,
    val semesters: List<OTLUserLectureSemester> = emptyList(),
    val timetables: Map<String, Timetable> = emptyMap(),
    val grades: Map<Int, LectureGrade> = emptyMap(),
    val majorDepartments: List<Department> = emptyList(),
    val requirements: CreditRequirements = CreditRequirements(),
) {
    private val lectures get() = semesters.flatMap { timetables[it.id]?.lectures.orEmpty() }
    private val countedLectures get() = RetakeResolver.countedLectures(lectures, grades)
    val supersededLectureIDs get() = RetakeResolver.supersededLectureIDs(lectures, grades)
    val overallSummary get() = SemesterGradeSummary.calculate(countedLectures, grades)
    val creditBreakdown get() = CreditBreakdown(countedLectures, grades, majorDepartments)
    fun summary(item: OTLUserLectureSemester): SemesterGradeSummary? =
        timetables[item.id]?.let { SemesterGradeSummary.calculate(it.lectures, grades) }
}

interface CreditCalculationViewModelProtocol {
    val state: StateFlow<CreditCalculationViewState>
    fun load(forceRefresh: Boolean = false)
    fun refresh()
    fun setGrade(grade: LectureGrade?, lectureID: Int)
    fun updateRequirements(requirements: CreditRequirements)
    val alertState: AlertState?
    var isAlertPresented: Boolean
}

@HiltViewModel
class CreditCalculationViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCaseProtocol,
    private val timetableUseCase: TimetableUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val lectureGradeUseCase: LectureGradeUseCaseProtocol,
    private val creditSummaryPublisher: CreditSummaryPublisher,
) : ViewModel(), CreditCalculationViewModelProtocol {
    private val mutableState = MutableStateFlow(CreditCalculationViewState())
    override val state = mutableState.asStateFlow()
    override var alertState: AlertState? by mutableStateOf(null)
        private set
    override var isAlertPresented: Boolean by mutableStateOf(false)
    private var userID: Int? = null
    private var loadJob: Job? = null
    private val saveMutex = Mutex()
    private val sessionRevision = creditSummaryPublisher.revision

    init {
        load()
    }

    override fun refresh() = load(forceRefresh = true)

    override fun load(forceRefresh: Boolean) {
        if (loadJob?.isActive == true || sessionRevision != creditSummaryPublisher.revision) return
        val previous = state.value
        val keepsContent = forceRefresh && !previous.isLoading && previous.error == null
        loadJob = viewModelScope.launch {
            if (!keepsContent) mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                if (userID == null || userUseCase.otlUser == null) userUseCase.fetchOTLUser()
                val user = checkNotNull(userUseCase.otlUser)
                if (sessionRevision != creditSummaryPublisher.revision || (userID != null && userID != user.id)) return@launch
                userID = user.id
                coroutineScope {
                    val history = async { lectureUseCase.fetchUserLectureHistory(user.id) }
                    val available =
                        async { (if (forceRefresh) timetableUseCase.refreshSemesters() else timetableUseCase.getSemesters()).associateBy { it.id } }
                    val semesters = history.await().semesters.filter { it.lectures.isNotEmpty() }
                        .distinctBy { it.id }
                        .sortedWith(compareBy({ it.year }, { it.semesterType.intValue }))
                    val semestersByID = available.await()
                    val limiter = Semaphore(4)
                    val timetables = semesters.map { item ->
                        async {
                            limiter.withPermit {
                                val fallback = previous.timetables[item.id].takeIf { keepsContent }
                                loadTimetable(semestersByID[item.id], fallback)?.let { item.id to it }
                            }
                        }
                    }.awaitAll().filterNotNull().toMap()
                    saveMutex.withLock {
                        if (!isCurrentSession(user.id)) return@withLock
                        val grades = lectureGradeUseCase.grades(user.id)
                        val requirements = lectureGradeUseCase.requirements(user.id)
                        if (!isCurrentSession(user.id)) return@withLock
                        mutableState.value = CreditCalculationViewState(
                            isLoading = false,
                            semesters = semesters,
                            timetables = timetables,
                            grades = grades,
                            requirements = requirements,
                            majorDepartments = user.majorDepartments,
                        )
                        publishWidgetSnapshot()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Timber.e(error, "Could not load credit history")
                if (!keepsContent && sessionRevision == creditSummaryPublisher.revision) {
                    mutableState.update { it.copy(isLoading = false, error = error) }
                }
            }
        }
    }

    private suspend fun loadTimetable(semester: Semester?, fallback: Timetable?): Timetable? {
        if (semester == null) return fallback
        return try {
            timetableUseCase.getMyTable(semester, forceRefresh = true)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            fallback ?: throw error
        }
    }

    override fun setGrade(grade: LectureGrade?, lectureID: Int) {
        val accountID = userID?.takeIf(::isCurrentSession) ?: return
        val lecture =
            state.value.timetables.values.asSequence().flatMap { it.lectures }.find { it.id == lectureID }
                ?: return
        if (grade != null && grade !in LectureGrade.options(lecture)) return
        save(
            accountID = accountID,
            persist = { lectureGradeUseCase.setGrade(grade, lectureID, accountID) },
            updateState = { it.copy(grades = if (grade == null) it.grades - lectureID else it.grades + (lectureID to grade)) },
        )
    }

    override fun updateRequirements(requirements: CreditRequirements) {
        val accountID = userID?.takeIf(::isCurrentSession) ?: return
        if (!requirements.isValid) return
        save(
            accountID = accountID,
            persist = { lectureGradeUseCase.saveRequirements(requirements, accountID) },
            updateState = { it.copy(requirements = requirements) },
        )
    }

    private fun save(
        accountID: Int,
        persist: suspend () -> Unit,
        updateState: (CreditCalculationViewState) -> CreditCalculationViewState,
    ) {
        viewModelScope.launch {
            saveMutex.withLock {
                if (!isCurrentSession(accountID)) return@withLock
                try {
                    persist()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Timber.e(error, "Could not save credit data")
                    if (isCurrentSession(accountID)) {
                        alertState = error.toAlertState(R.string.credit_save_error)
                        isAlertPresented = true
                    }
                    return@withLock
                }
                if (!isCurrentSession(accountID)) return@withLock
                mutableState.update(updateState)
                publishWidgetSnapshot()
            }
        }
    }

    private suspend fun publishWidgetSnapshot() {
        val current = state.value
        if (current.isLoading || current.error != null || userID?.let(::isCurrentSession) != true) return
        if (current.semesters.any { it.id !in current.timetables }) return
        val summary = current.overallSummary
        try {
            creditSummaryPublisher.publish(
                CreditSummarySnapshot(
                    summary.gpa, summary.earnedCredits, current.requirements.graduation
                ),
                sessionRevision,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Timber.e(error, "Could not publish credit summary")
        }
    }

    private fun isCurrentSession(accountID: Int): Boolean =
        userUseCase.otlUser?.id == accountID && sessionRevision == creditSummaryPublisher.revision

}
