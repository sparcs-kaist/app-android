package org.sparcs.soap.App.Cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/// Data model that stores a serialised Timetable for offline / cached access.
///
/// Two kinds of timetable are cached:
///  - by timetable ID    → `cacheKey = "\(timetableID)"`
///  - "my table"         → `cacheKey = "\(year)-\(semesterRawValue)-myTable"`
@Entity(tableName = "cached_timetables")
class CachedTimetable(
    /// Unique lookup key – matches the `Timetable.id` produced by the repository.
    @PrimaryKey
    var cacheKey: String,

    /// JSON-encoded `Timetable`.
    var data: ByteArray,

    /// When this entry was last written.
    var updatedAt: Date = Date()
)