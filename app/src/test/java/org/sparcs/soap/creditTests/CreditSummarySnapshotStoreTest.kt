package org.sparcs.soap.creditTests

import android.app.Application
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCase

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CreditSummarySnapshotStoreTest {
    @Test fun summarySurvivesRecreationAndClearingKeepsAccountGrades() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val grades = LectureGradeUseCase(context)
        grades.setGrade(LectureGrade.A_PLUS, 42, 7)
        val snapshot = CreditSummarySnapshot(4.3, 3, 138, 1234)
        val store = CreditSummarySnapshotStore(context)
        store.save(snapshot)
        assertEquals(snapshot, CreditSummarySnapshotStore(context).snapshot)
        store.clear()
        assertNull(CreditSummarySnapshotStore(context).snapshot)
        assertEquals(LectureGrade.A_PLUS, grades.grades(7)[42])
        assertTrue(grades.grades(8).isEmpty())
    }

    @Test fun unchangedValuesIgnoreTimestamp() {
        val snapshot = CreditSummarySnapshot(null, 0, 0, 1)
        assertTrue(snapshot.hasSameValues(snapshot.copy(updatedAt = 2)))
    }
}
