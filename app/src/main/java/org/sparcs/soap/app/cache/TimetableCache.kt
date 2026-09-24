package org.sparcs.soap.app.cache
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCachedState
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/// Thread-safe cache that reads/writes ``CachedTimetable`` records from Room.
@Singleton
class TimetableCache @Inject constructor(
    private val timetableCacheDao: TimetableCacheDAO,
    private val gson: Gson = Gson()
) {

    private val mutex = Mutex()
    @Volatile private var generation = 0L

    private class Session(val generation: Long) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Session>
    }

    internal suspend fun sessionContext(): CoroutineContext = currentCoroutineContext()[Session] ?: Session(generation)

    suspend fun <T> withSession(block: suspend () -> T): T {
        val session = currentCoroutineContext()[Session] ?: Session(generation)
        return withContext(session) {
            if (session.generation != generation) throw CancellationException("Timetable session changed")
            val result = block()
            if (session.generation != generation) throw CancellationException("Timetable session changed")
            result
        }
    }

    private suspend fun persist(entry: CachedTimetable) {
        val session = currentCoroutineContext()[Session]
        mutex.withLock {
            if (session != null && session.generation != generation) throw CancellationException("Timetable session changed")
            timetableCacheDao.saveTimetable(entry)
        }
    }

    // MARK: - Read

    /// Returns the cached `Timetable` for the given key, or `nil` if not found.
    suspend fun timetable(key: String): Timetable? {
        val cached = timetableCacheDao.getTimetable(key) ?: return null

        return try {
            gson.fromJson(String(cached.data, Charsets.UTF_8), Timetable::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun semesters(): List<Semester>? = read("semesters", Array<Semester>::class.java)?.toList()
    suspend fun storeSemesters(value: List<Semester>) = write(value, "semesters")
    suspend fun currentSemester(): Semester? = read("current-semester", Semester::class.java)
    suspend fun storeCurrentSemester(value: Semester) {
        write(value, "current-semester")
        val table = timetableCacheDao.getTimetable("${value.id}-myTable")
        if (table != null) persist(CachedTimetable("current-myTable", table.data, table.updatedAt))
        else invalidate("current-myTable")
    }

    suspend fun currentMyTable(): Timetable? = timetable("current-myTable")
    suspend fun storeCurrentMyTable(value: Timetable, semester: Semester) {
        store(value, "${semester.id}-myTable")
        store(value, "current-myTable")
    }

    suspend fun timetableSummaries(semester: Semester): List<TimetableSummary>? = read("${semester.id}-summaries", Array<TimetableSummary>::class.java)?.toList()
    suspend fun storeTimetableSummaries(value: List<TimetableSummary>, semester: Semester) =
        write(value, "${semester.id}-summaries")

    suspend fun updateTimetableSummary(id: Int, title: String?) {
        timetableCacheDao.getSummaries().forEach { record ->
            val summaries = read(record.cacheKey, Array<TimetableSummary>::class.java)?.toList() ?: return@forEach
            val updated = if (title == null) summaries.filterNot { it.id == id }
                else summaries.map { if (it.id == id) it.copy(title = title) else it }
            write(updated, record.cacheKey)
        }
    }

    suspend fun state(semester: Semester?, timetableID: Int?): TimetableCachedState {
        val key = timetableID?.toString() ?: semester?.let { "${it.id}-myTable" }
        val table = key?.let { timetable(it) }
        return TimetableCachedState(
            semesters(), currentSemester(), semester?.let { timetableSummaries(it) }, table,
            if (table != null) key?.let { timetableCacheDao.getTimetable(it)?.updatedAt } else null,
        )
    }

    suspend fun clear() = mutex.withLock {
        generation++
        timetableCacheDao.clear()
    }

    private suspend fun <T> read(key: String, type: Class<T>): T? {
        val record = timetableCacheDao.getTimetable(key) ?: return null
        return try {
            gson.fromJson<T>(String(record.data, Charsets.UTF_8), type)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun write(value: Any, key: String) {
        persist(CachedTimetable(key, gson.toJson(value).toByteArray(Charsets.UTF_8), Date()))
    }

    // MARK: - Write

    /// Persists a `Timetable` under the given key, inserting or updating as needed.
    suspend fun store(timetable: Timetable, key: String) {
        val jsonString = try {
            gson.toJson(timetable)
        } catch (_: Exception) {
            return
        }

        val data = jsonString.toByteArray()
        val entry = CachedTimetable(
            cacheKey = key,
            data = data,
            updatedAt = Date()
        )

        persist(entry)
    }

    // MARK: - Invalidate
    /// Removes the cached entry for the given key.
    suspend fun invalidate(key: String) {
        val session = currentCoroutineContext()[Session]
        mutex.withLock {
            if (session != null && session.generation != generation) throw CancellationException("Timetable session changed")
            timetableCacheDao.invalidate(key)
        }
    }
}
