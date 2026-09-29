package org.sparcs.soap.app.features.timetable.creditCalculation

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
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.models.otl.CreditBreakdown
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.domain.models.otl.Department
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.RetakeResolver
import org.sparcs.soap.app.domain.models.otl.SemesterGradeSummary
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import javax.inject.Inject

data class CreditCalculationViewState(
    val isLoading: Boolean = true,
    val error: Exception? = null,
    val semesters: List<OTLUserLectureSemester> = emptyList(),
    val timetables: Map<String, Timetable> = emptyMap(),
    val grades: Map<Int, LectureGrade> = emptyMap(),
    val majorDepartments: List<Department> = emptyList(),
    val requirements: CreditRequirements = CreditRequirements(),
    val saveError: Boolean = false,
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
    fun dismissSaveError()
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
                                val semester = semestersByID[item.id] ?: return@withPermit null
                                val table = try {
                                    timetableUseCase.getMyTable(semester, forceRefresh = true)
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    if (keepsContent) previous.timetables[item.id]
                                        ?: throw error else throw error
                                }
                                item.id to table
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
                if (!keepsContent && sessionRevision == creditSummaryPublisher.revision) {
                    mutableState.update { it.copy(isLoading = false, error = error) }
                }
            }
        }
    }

    override fun setGrade(grade: LectureGrade?, lectureID: Int) {
        val accountID = userID?.takeIf(::isCurrentSession) ?: return
        val lecture =
            state.value.timetables.values.flatMap { it.lectures }.find { it.id == lectureID }
                ?: return
        if (grade != null && grade !in LectureGrade.options(lecture)) return
        viewModelScope.launch {
            saveMutex.withLock {
                if (!isCurrentSession(accountID)) return@withLock
                try {
                    lectureGradeUseCase.setGrade(grade, lectureID, accountID)
                    if (!isCurrentSession(accountID)) return@withLock
                    mutableState.update {
                        it.copy(grades = if (grade == null) it.grades - lectureID else it.grades + (lectureID to grade))
                    }
                    publishWidgetSnapshot()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    if (isCurrentSession(accountID)) mutableState.update { it.copy(saveError = true) }
                }
            }
        }
    }

    override fun updateRequirements(requirements: CreditRequirements) {
        val accountID = userID?.takeIf(::isCurrentSession) ?: return
        if (!requirements.isValid) return
        viewModelScope.launch {
            saveMutex.withLock {
                if (!isCurrentSession(accountID)) return@withLock
                try {
                    lectureGradeUseCase.saveRequirements(requirements, accountID)
                    if (!isCurrentSession(accountID)) return@withLock
                    mutableState.update { it.copy(requirements = requirements) }
                    publishWidgetSnapshot()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    if (isCurrentSession(accountID)) mutableState.update { it.copy(saveError = true) }
                }
            }
        }
    }

    private suspend fun publishWidgetSnapshot() {
        val current = state.value
        if (current.isLoading || current.error != null || userID?.let(::isCurrentSession) != true) return
        if (current.timetables.isEmpty()) return
        val summary = current.overallSummary
        creditSummaryPublisher.publish(
            CreditSummarySnapshot(
                summary.gpa, summary.earnedCredits, current.requirements.graduation
            ),
            sessionRevision,
        )
    }

    private fun isCurrentSession(accountID: Int): Boolean =
        userUseCase.otlUser?.id == accountID && sessionRevision == creditSummaryPublisher.revision

    override fun dismissSaveError() {
        mutableState.update { it.copy(saveError = false) }
    }
}
