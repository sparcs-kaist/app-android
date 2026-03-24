package org.sparcs.soap.App.Cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import java.util.Date

@Dao
interface TaxiRouteCacheDAO {

    @Query("SELECT * FROM cached_taxiRoute WHERE cacheKey = :key LIMIT 1")
    suspend fun getRoute(key: String): CachedTaxiRoute?

    @Upsert
    suspend fun saveRoute(route: CachedTaxiRoute)

    @Query("DELETE FROM cached_taxiRoute WHERE cacheKey = :key")
    suspend fun invalidate(key: String)

    @Query("DELETE FROM cached_taxiRoute WHERE updatedAt < :expiredDate")
    suspend fun deleteExpired(expiredDate: Date)
}