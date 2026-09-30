package org.sparcs.soap.app.domain.usecases.otl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface LectureGradeUseCaseProtocol {
    suspend fun grades(userID: Int): Map<Int, LectureGrade>
    suspend fun setGrade(grade: LectureGrade?, lectureID: Int, userID: Int)
    suspend fun requirements(userID: Int): CreditRequirements
    suspend fun saveRequirements(requirements: CreditRequirements, userID: Int)
}

@Singleton
class LectureGradeUseCase @Inject constructor(@ApplicationContext context: Context) : LectureGradeUseCaseProtocol {
    private val preferences = context.getSharedPreferences("lecture_grades", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun grades(userID: Int): Map<Int, LectureGrade> = withContext(Dispatchers.IO) {
        val prefix = "grade.$userID."
        preferences.all.mapNotNull { (key, value) ->
            val lectureID = key.takeIf { it.startsWith(prefix) }?.removePrefix(prefix)?.toIntOrNull()
            val grade = LectureGrade.entries.find { it.rawValue == value }
            if (lectureID != null && grade != null) lectureID to grade else null
        }.toMap()
    }

    override suspend fun setGrade(grade: LectureGrade?, lectureID: Int, userID: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val key = "grade.$userID.$lectureID"
            val editor = preferences.edit()
            if (grade == null) editor.remove(key) else editor.putString(key, grade.rawValue)
            if (!editor.commit()) throw IOException("Could not save lecture grade")
        }
    }

    override suspend fun requirements(userID: Int): CreditRequirements = withContext(Dispatchers.IO) {
        preferences.getString("requirements.$userID", null)?.let { value ->
            runCatching { json.decodeFromString<CreditRequirements>(value) }.getOrNull()
                ?.takeIf { it.isValid }
        } ?: CreditRequirements()
    }

    override suspend fun saveRequirements(requirements: CreditRequirements, userID: Int) = withContext(Dispatchers.IO) {
        require(requirements.isValid)
        mutex.withLock {
            if (!preferences.edit().putString("requirements.$userID", json.encodeToString(requirements)).commit()) {
                throw IOException("Could not save credit requirements")
            }
        }
    }
}
