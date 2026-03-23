package org.sparcs.soap.App.Cache
import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/// Thread-safe cache that reads/writes ``CachedTimetable`` records from Room.
@Singleton
class TimetableCache @Inject constructor(
    private val timetableCacheDao: TimetableCacheDAO,
    private val gson: Gson = Gson()
) {

    // MARK: - Read

    /// Returns the cached `Timetable` for the given key, or `nil` if not found.
    suspend fun timetable(key: String): Timetable? {
        val cached = timetableCacheDao.getTimetable(key) ?: return null

        return try {
            gson.fromJson(String(cached.data, Charsets.UTF_8), Timetable::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // MARK: - Write

    /// Persists a `Timetable` under the given key, inserting or updating as needed.
    suspend fun store(timetable: Timetable, key: String) {
        val jsonString = try {
            gson.toJson(timetable)
        } catch (e: Exception) {
            return
        }

        val data = jsonString.toByteArray()
        val entry = CachedTimetable(
            cacheKey = key,
            data = data,
            updatedAt = Date()
        )

        timetableCacheDao.saveTimetable(entry)
    }

    // MARK: - Invalidate
    /// Removes the cached entry for the given key.
    suspend fun invalidate(key: String) {
        timetableCacheDao.invalidate(key)
    }
}