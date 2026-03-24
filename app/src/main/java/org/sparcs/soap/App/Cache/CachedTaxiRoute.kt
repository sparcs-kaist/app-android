package org.sparcs.soap.App.Cache

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