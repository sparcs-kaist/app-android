package org.sparcs.soap.App.Cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TimetableCacheDAO {
    @Query("SELECT * FROM cached_timetables WHERE cacheKey = :key LIMIT 1")
    suspend fun getTimetable(key: String): CachedTimetable?

    @Upsert
    suspend fun saveTimetable(timetable: CachedTimetable)

    @Query("DELETE FROM cached_timetables WHERE cacheKey = :key")
    suspend fun invalidate(key: String)
}