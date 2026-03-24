package org.sparcs.soap.App.Cache

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxiRouteCache @Inject constructor(
    private val taxiRouteCacheDao: TaxiRouteCacheDAO,
    private val gson: Gson = Gson(),
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val retentionCutoffDate  = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            taxiRouteCacheDao.deleteExpired(retentionCutoffDate )
        }
    }

    suspend fun getRoute(key: String): List<LatLng>? {
        val cached = taxiRouteCacheDao.getRoute(key) ?: return null
        return try {
            val type = object : TypeToken<List<LatLng>>() {}.type
            val jsonString = String(cached.data, Charsets.UTF_8)
            gson.fromJson<List<LatLng>>(jsonString, type)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun store(key: String, points: List<LatLng>) {
        val jsonString = gson.toJson(points)
        val entry = CachedTaxiRoute(
            cacheKey = key,
            data = jsonString.toByteArray(Charsets.UTF_8),
            updatedAt = Date()
        )
        taxiRouteCacheDao.saveRoute(entry)
    }

    suspend fun invalidate(key: String) {
        taxiRouteCacheDao.invalidate(key)
    }
}