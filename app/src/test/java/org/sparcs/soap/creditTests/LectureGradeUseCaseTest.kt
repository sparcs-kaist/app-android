package org.sparcs.soap.creditTests

import android.content.Context
import android.app.Application
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCase

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class LectureGradeUseCaseTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before fun reset() { context.getSharedPreferences("lecture_grades", Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun `grades survive store recreation and stay isolated by user`() = runTest {
        val store = LectureGradeUseCase(context)
        store.setGrade(LectureGrade.A_PLUS, 10, 1)
        store.setGrade(LectureGrade.FAIL, 10, 2)
        val reopened = LectureGradeUseCase(context)
        assertEquals(mapOf(10 to LectureGrade.A_PLUS), reopened.grades(1))
        assertEquals(mapOf(10 to LectureGrade.FAIL), reopened.grades(2))
        reopened.setGrade(null, 10, 1)
        assertTrue(reopened.grades(1).isEmpty())
        assertEquals(LectureGrade.FAIL, reopened.grades(2)[10])
    }

    @Test fun `requirements persist per user independently of grades`() = runTest {
        val store = LectureGradeUseCase(context)
        val requirements = CreditRequirements(graduation = 140, majorRequired = mapOf(3 to 25))
        store.saveRequirements(requirements, 1)
        assertEquals(requirements, LectureGradeUseCase(context).requirements(1))
        assertEquals(CreditRequirements(), store.requirements(2))
    }
}
