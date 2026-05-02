package org.sparcs.soap.Widgets.AraPortalWidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.sparcs.soap.App.Domain.Enums.Ara.AraPortalNoticeType
import org.sparcs.soap.App.Domain.Enums.Ara.PostListType
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.WidgetEntryPoint
import timber.log.Timber

class AraPortalUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WidgetEntryPoint::class.java
        )
        val tokenStorage = entryPoint.tokenStorage()
        val syncManager = entryPoint.araPortalSyncManager()
        val araBoardUseCase = entryPoint.araBoardUseCase()

        return try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(AraPortalWidget::class.java)
            if (glanceIds.isEmpty()) return Result.success()

            if (tokenStorage.getAccessToken() == null || tokenStorage.isTokenExpired()) {
                Timber.tag("AraPortalWidget").d("Token missing or expired, requesting sign in")
                syncManager.sync(AraPortalUiState(signInRequired = true, isLoading = false))
                return Result.success()
            }

            glanceIds.forEach { glanceId ->
                Timber.tag("AraPortalWidget").d("Setting loading state for $glanceId")
                syncManager.sync(glanceId, AraPortalUiState(isLoading = true))
            }

            val boardEntriesCache = mutableMapOf<Int, List<WidgetNoticeEntry>>()
            val keywordEntriesCache = mutableMapOf<String, List<WidgetNoticeEntry>>()
            var trendingCache: List<WidgetNoticeEntry>? = null

            glanceIds.forEach { glanceId ->
                try {
                    var settings = AraPortalWidgetSettings()
                    updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        settings = prefs.toAraPortalWidgetSettings()
                        prefs
                    }

                    Timber.tag("AraPortalWidget").d("Updating glanceId: $glanceId, showTrending: ${settings.showTrending}, boards: ${settings.selectedBoardIds}")

                    val selectedIds = settings.selectedBoardIds.takeIf { it.isNotEmpty() }
                        ?: emptySet()

                    val trendingEntries = if (settings.showTrending) {
                        if (trendingCache == null) {
                            coroutineScope {
                                val portalTrendingDeferred = async {
                                    runCatching {
                                        araBoardUseCase.fetchTrendingPortalNotices()
                                            .take(5)
                                            .map { it.toWidgetEntry(applicationContext.getString(R.string.ara_portal_widget_trending_label)) }
                                    }.getOrElse { emptyList() }
                                }

                                val araTrendingDeferred = async {
                                    runCatching {
                                        araBoardUseCase.fetchPosts(
                                            type = PostListType.All,
                                            page = 1,
                                            pageSize = 5,
                                            searchKeyword = null
                                        ).results.take(5).map {
                                            it.toWidgetEntry(applicationContext.getString(R.string.ara_portal_widget_trending_label))
                                        }
                                    }.getOrElse { emptyList() }
                                }

                                trendingCache = (portalTrendingDeferred.await() + araTrendingDeferred.await()).distinctBy { it.id }
                                Timber.tag("AraPortalWidget").d("Trending cache initialized: ${trendingCache.size} items")
                            }
                        }
                        trendingCache!!
                    } else {
                        emptyList()
                    }

                    val boardEntries = if (!settings.showTrending) {
                        coroutineScope {
                            selectedIds.map { boardId ->
                                async {
                                    boardEntriesCache.getOrPut(boardId) {
                                        runCatching {
                                            val noticeType = AraPortalNoticeType.fromId(boardId)
                                            val isPortalNotice = noticeType != AraPortalNoticeType.Unknown

                                            if (isPortalNotice) {
                                                val araPortalNoticeTypeName = noticeType.localizedString(applicationContext)
                                                val pResults = araBoardUseCase.fetchPortalNotices(boardId, page = 1, pageSize = 5)
                                                Timber.tag("AraPortalWidget").d("Fetched via PortalNotice for $boardId ($noticeType): ${pResults.size} items")
                                                pResults.take(5).map { it.toWidgetEntry(araPortalNoticeTypeName) }
                                            } else {
                                                // Regular Ara board
                                                val results = araBoardUseCase.fetchPosts(
                                                    type = PostListType.Board(boardId),
                                                    page = 1,
                                                    pageSize = 5,
                                                    searchKeyword = null
                                                ).results

                                                if (results.isNotEmpty()) {
                                                    Timber.tag("AraPortalWidget").d("Fetched via fetchPosts for $boardId: ${results.size} items")
                                                    results.map { it.toWidgetEntry() }
                                                } else {
                                                    emptyList()
                                                }
                                            }
                                        }.getOrElse { 
                                            Timber.tag("AraPortalWidget").e(it, "Failed to fetch for board $boardId")
                                            emptyList() 
                                        }
                                    }
                                }
                            }.awaitAll().flatten()
                        }
                    } else {
                        emptyList()
                    }

                    val keywordEntries = if (!settings.showTrending && settings.keywordEnabled && settings.keywords.isNotEmpty()) {
                        coroutineScope {
                            settings.keywords.map { keyword ->
                                async {
                                    keywordEntriesCache.getOrPut(keyword) {
                                        runCatching {
                                            araBoardUseCase.fetchPosts(
                                                type = PostListType.All,
                                                page = 1,
                                                pageSize = 5,
                                                searchKeyword = keyword
                                            ).results.take(5).map { it.toWidgetEntry() }
                                        }.getOrElse { emptyList() }
                                    }
                                }
                            }.awaitAll().flatten()
                        }
                    } else {
                        emptyList()
                    }

                    val allCandidateEntries = (trendingEntries + boardEntries + keywordEntries).distinctBy { it.id }
                    Timber.tag("AraPortalWidget").d("Total candidates: ${allCandidateEntries.size}")

                    val notices = allCandidateEntries
                        .sortedWith(
                            compareByDescending<WidgetNoticeEntry> { entry ->
                                trendingEntries.any { it.id == entry.id }
                            }.thenByDescending { it.id }
                        )
                        .take(10)

                    Timber.tag("AraPortalWidget").d("Syncing ${notices.size} notices to glanceId $glanceId (isLoading=false)")

                    syncManager.sync(
                        glanceId = glanceId,
                        state = AraPortalUiState(
                            notices = notices,
                            signInRequired = false,
                            isLoading = false,
                            showTrending = settings.showTrending,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    Timber.tag("AraPortalWidget").e(e, "failed to update glanceId $glanceId. resetting loading state.")
                    syncManager.sync(glanceId, AraPortalUiState(isLoading = false))
                }
            }
            Timber.tag("AraPortalWidget").d("All widgets updated. calling updateAll.")
            AraPortalWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.tag("AraPortalWidget").e(e, "update failed")
            Result.retry()
        }
    }
}
