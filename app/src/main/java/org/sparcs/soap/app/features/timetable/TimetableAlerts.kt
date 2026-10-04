package org.sparcs.soap.app.features.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog

@Composable
internal fun TimetableAlerts(navController: NavController) {
    val entry by navController.currentBackStackEntryAsState()
    if (entry?.destination?.hierarchy?.none { it.route == "OTLGraph" } != false) return
    val tableEntry = remember(entry) { navController.getBackStackEntry("OTLGraph") }
    val table: TimetableViewModel = hiltViewModel(tableEntry)
    GlobalAlertDialog(table.isAlertPresented, table.alertState) { table.isAlertPresented = false }

    val searchEntry = remember(entry) {
        runCatching { navController.getBackStackEntry("LectureSearchGraph") }.getOrNull()
    }
    if (searchEntry != null) {
        val search: LectureSearchViewModel = hiltViewModel(searchEntry)
        GlobalAlertDialog(search.isAlertPresented, search.alertState) { search.isAlertPresented = false }
    }
}
