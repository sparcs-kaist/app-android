package org.sparcs.soap.app.cache

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
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
            val retentionCutoffDate = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            taxiRouteCacheDao.deleteExpired(retentionCutoffDate)
        }
    }

    suspend fun getRoute(key: String): TaxiRoute? {
        val cached = taxiRouteCacheDao.getRoute(key) ?: return null
        return runCatching {
            gson.fromJson(String(cached.data, Charsets.UTF_8), TaxiRoute::class.java)
        }.getOrNull()?.takeIf { it.points.isNotEmpty() && it.durationSeconds > 0 }
    }

    suspend fun store(key: String, route: TaxiRoute) {
        val entry = CachedTaxiRoute(
            cacheKey = key,
            data = gson.toJson(route).toByteArray(Charsets.UTF_8),
            updatedAt = Date()
        )
        taxiRouteCacheDao.saveRoute(entry)
    }

    suspend fun invalidate(key: String) {
        taxiRouteCacheDao.invalidate(key)
    }
}
