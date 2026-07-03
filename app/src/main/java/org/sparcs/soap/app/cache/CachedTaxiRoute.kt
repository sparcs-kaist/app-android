package org.sparcs.soap.app.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cached_taxiRoute")
class CachedTaxiRoute(
    @PrimaryKey
    var cacheKey: String,

    var data: ByteArray,

    var updatedAt: Date = Date(),
)