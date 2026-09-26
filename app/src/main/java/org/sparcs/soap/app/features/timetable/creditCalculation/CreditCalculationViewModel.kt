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
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCase
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

@HiltViewModel
class CreditCalculationViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCaseProtocol,
    private val timetableUseCase: TimetableUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val lectureGradeUseCase: LectureGradeUseCase,
    private val creditSummaryPublisher: CreditSummaryPublisher,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CreditCalculationViewState())
    val state = mutableState.asStateFlow()
    private var userID: Int? = null
    private var loadJob: Job? = null
    private val saveMutex = Mutex()
    private var sessionRevision = creditSummaryPublisher.revision

    init { load() }

    fun refresh() = load(forceRefresh = true)

    fun load(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        val previous = state.value
        val keepsContent = forceRefresh && !previous.isLoading && previous.error == null
        loadJob = viewModelScope.launch {
            if (!keepsContent) mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                if (userUseCase.otlUser == null) userUseCase.fetchOTLUser()
                val user = checkNotNull(userUseCase.otlUser)
                userID = user.id
                sessionRevision = creditSummaryPublisher.revision
                val grades = lectureGradeUseCase.grades(user.id)
                val requirements = lectureGradeUseCase.requirements(user.id)
                coroutineScope {
                    val history = async { lectureUseCase.fetchUserLectureHistory(user.id) }
                    val available = async { (if (forceRefresh) timetableUseCase.refreshSemesters() else timetableUseCase.getSemesters()).associateBy { it.id } }
                    val semesters = history.await().semesters.filter { it.lectures.isNotEmpty() }
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
                                    if (keepsContent) previous.timetables[item.id] ?: throw error else throw error
                                }
                                item.id to table
                            }
                        }
                    }.awaitAll().filterNotNull().toMap()
                    saveMutex.withLock {
                        if (userUseCase.otlUser?.id != user.id || sessionRevision != creditSummaryPublisher.revision) return@withLock
                        mutableState.value = CreditCalculationViewState(
                            isLoading = false,
                            semesters = semesters,
                            timetables = timetables,
                            grades = if (keepsContent) state.value.grades else grades,
                            requirements = if (keepsContent) state.value.requirements else requirements,
                            majorDepartments = user.majorDepartments,
                        )
                        publishWidgetSnapshot()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (!keepsContent) mutableState.update { it.copy(isLoading = false, error = error) }
            }
        }
    }

    fun setGrade(grade: LectureGrade?, lectureID: Int) {
        val accountID = userID?.takeIf { it == userUseCase.otlUser?.id } ?: return
        val lecture = state.value.timetables.values.flatMap { it.lectures }.find { it.id == lectureID } ?: return
        if (grade != null && grade !in LectureGrade.options(lecture)) return
        viewModelScope.launch {
            saveMutex.withLock {
                if (userUseCase.otlUser?.id != accountID) return@withLock
                try {
                    lectureGradeUseCase.setGrade(grade, lectureID, accountID)
                    mutableState.update {
                        it.copy(grades = if (grade == null) it.grades - lectureID else it.grades + (lectureID to grade))
                    }
                    publishWidgetSnapshot()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(saveError = true) }
                }
            }
        }
    }

    fun updateRequirements(requirements: CreditRequirements) {
        val accountID = userID?.takeIf { it == userUseCase.otlUser?.id } ?: return
        if (!requirements.isValid) return
        viewModelScope.launch {
            saveMutex.withLock {
                if (userUseCase.otlUser?.id != accountID) return@withLock
                try {
                    lectureGradeUseCase.saveRequirements(requirements, accountID)
                    mutableState.update { it.copy(requirements = requirements) }
                    publishWidgetSnapshot()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(saveError = true) }
                }
            }
        }
    }

    private suspend fun publishWidgetSnapshot() {
        val current = state.value
        if (current.isLoading || current.error != null || userID != userUseCase.otlUser?.id) return
        val summary = current.overallSummary
        creditSummaryPublisher.publish(
            CreditSummarySnapshot(summary.gpa, summary.earnedCredits, current.requirements.graduation),
            sessionRevision,
        )
    }

    fun dismissSaveError() { mutableState.update { it.copy(saveError = false) } }
}
