package org.sparcs.soap.widgets.araPortalWidget

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
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraPortalNoticeType
import org.sparcs.soap.app.domain.enums.ara.PostListType
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.widgets.WidgetEntryPoint
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
                syncManager.sync(AraPortalUiState(signInRequired = true, isLoading = false))
                return Result.success()
            }

            glanceIds.forEach { glanceId ->
                syncManager.sync(glanceId, AraPortalUiState(isLoading = true))
            }

            val trendingLabel = applicationContext.getString(R.string.ara_portal_widget_trending_label)
            var trendingCache: List<WidgetNoticeEntry>? = null
            val boardEntriesCache = mutableMapOf<Int, List<WidgetNoticeEntry>>()
            val keywordEntriesCache = mutableMapOf<String, List<WidgetNoticeEntry>>()

            glanceIds.forEach { glanceId ->
                try {
                    var settings = AraPortalWidgetSettings()
                    updateAppWidgetState(
                        applicationContext,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        settings = prefs.toAraPortalWidgetSettings()
                        prefs
                    }

                    val trendingEntries = if (settings.showTrending) {
                        if (trendingCache == null) {
                            trendingCache = coroutineScope {
                                val portal = async {
                                    runCatching {
                                        araBoardUseCase.fetchTrendingPortalNotices()
                                            .take(5)
                                            .map { it.toWidgetEntry(trendingLabel) }
                                    }.getOrElse { emptyList() }
                                }
                                val posts = async {
                                    runCatching {
                                        araBoardUseCase.fetchPosts(
                                            type = PostListType.All,
                                            page = 1,
                                            pageSize = 5,
                                            searchKeyword = null
                                        ).results.take(5).map { it.toWidgetEntry(trendingLabel) }
                                    }.getOrElse { emptyList() }
                                }
                                (portal.await() + posts.await()).distinctBy { it.id }
                            }
                        }
                        trendingCache
                    } else {
                        emptyList()
                    }

                    val boardEntries = if (!settings.showTrending) {
                        coroutineScope {
                            settings.selectedBoardIds.map { boardId ->
                                async {
                                    boardEntriesCache.getOrPut(boardId) {
                                        fetchBoardEntries(araBoardUseCase, boardId)
                                    }
                                }
                            }.awaitAll().flatten()
                        }
                    } else {
                        emptyList()
                    }

                    val keywordEntries = if (
                        !settings.showTrending &&
                        settings.keywordEnabled &&
                        settings.keywords.isNotEmpty()
                    ) {
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

                    val notices = (trendingEntries + boardEntries + keywordEntries)
                        .distinctBy { it.id }
                        .sortedWith(
                            compareByDescending<WidgetNoticeEntry> { entry ->
                                trendingEntries.any { it.id == entry.id }
                            }.thenByDescending { it.id }
                        )
                        .take(10)

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
                    Timber.tag("AraPortalWidget").e(e, "failed to update glanceId $glanceId")
                    syncManager.sync(glanceId, AraPortalUiState(isLoading = false))
                }
            }
            AraPortalWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.tag("AraPortalWidget").e(e, "update failed")
            Result.retry()
        }
    }

    private suspend fun fetchBoardEntries(
        araBoardUseCase: AraBoardUseCaseProtocol,
        boardId: Int,
    ): List<WidgetNoticeEntry> = runCatching {
        val noticeType = AraPortalNoticeType.fromId(boardId)
        if (noticeType != AraPortalNoticeType.Unknown) {
            val label = noticeType.localizedString(applicationContext)
            araBoardUseCase.fetchPortalNotices(boardId, page = 1, pageSize = 5)
                .take(5)
                .map { it.toWidgetEntry(label) }
        } else {
            araBoardUseCase.fetchPosts(
                type = PostListType.Board(boardId),
                page = 1,
                pageSize = 5,
                searchKeyword = null
            ).results.map { it.toWidgetEntry() }
        }
    }.getOrElse {
        Timber.tag("AraPortalWidget").e(it, "Failed to fetch for board $boardId")
        emptyList()
    }
}
