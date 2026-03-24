package org.sparcs.soap.App.Cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Database(
    entities = [
        CachedTimetable::class,
        CachedTaxiRoute::class
    ], version = 1, exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableCacheDao(): TimetableCacheDAO
    abstract fun taxiRouteCacheDao(): TaxiRouteCacheDAO
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}