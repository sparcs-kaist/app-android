package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.sparcs.soap.app.features.lectureSearch.LecturePreviewToggle
import org.sparcs.soap.app.features.lectureSearch.LectureSearchPage
import org.sparcs.soap.app.features.lectureSearch.LectureSearchSession
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.timetable.TimetableViewModel

@Composable
internal fun LectureSearchDestination(
    navController: NavController,
    content: @Composable (
        LectureSearchViewModelProtocol?,
        @Composable () -> Unit,
        @Composable RowScope.() -> Unit,
        Boolean,
    ) -> Unit,
) {
    val entry = navController.currentBackStackEntry
    val searchEntry = remember(entry) {
        runCatching { navController.getBackStackEntry("LectureSearchGraph") }.getOrNull()
    }
    if (searchEntry == null) {
        content(null, {}, {}, false)
        return
    }
    val search: LectureSearchViewModel = hiltViewModel(searchEntry)
    val flexible = remember(entry) {
        runCatching { navController.getBackStackEntry(Channel.LectureSearch.name) }.isSuccess
    }
    if (!flexible) {
        content(search, {}, {}, false)
        return
    }
    val tableEntry = remember(entry) { navController.getBackStackEntry("OTLGraph") }
    val table: TimetableViewModel = hiltViewModel(tableEntry)
    val session: LectureSearchSession = hiltViewModel(searchEntry)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            LectureSearchPage(navController, table, search, session = session, inspector = { content(search, {}, { LecturePreviewToggle(session) }, false) })
        } else {
            val previewMaxHeight = (maxHeight - 64.dp).coerceAtLeast(0.dp) * 0.6f
            content(search, {
                AnimatedVisibility(session.showPreview) {
                    LectureSearchTimetablePreview(
                        table, session.previewHeight.dp, previewMaxHeight,
                        { session.resizePreview(it.value) },
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }, { LecturePreviewToggle(session) }, session.previewHeight.dp.coerceIn(
                minOf(120.dp, previewMaxHeight), previewMaxHeight
            ) < 260.dp)
        }
    }
}
