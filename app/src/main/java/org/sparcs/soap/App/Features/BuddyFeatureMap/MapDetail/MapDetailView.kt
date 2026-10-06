package org.sparcs.soap.App.Features.BuddyFeatureMap.MapDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Enums.Map.MapTab
import org.sparcs.soap.App.Features.BuddyFeatureMap.ClassroomsView.ClassroomsView
import org.sparcs.soap.App.Features.BuddyFeatureMap.MapExplorer.Components.MapTabBar
import org.sparcs.soap.App.Features.BuddyFeatureMap.Nearby.NearbyView
import org.sparcs.soap.App.Features.BuddyFeatureMap.OLEV.OlevView
import org.sparcs.soap.App.Features.BuddyFeatureMap.Search.SearchView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDetailView(
    sheetState: SheetState,
    onDetentChange: (SheetValue) -> Unit
) {
    var selectedTab by remember { mutableStateOf(MapTab.Nearby) }

    LaunchedEffect(selectedTab) {
        if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
            sheetState.expand()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            when (selectedTab) {
                MapTab.Nearby -> NearbyView()
                MapTab.Classrooms -> ClassroomsView()
                MapTab.Olev -> OlevView()
                MapTab.Search -> SearchView({})
            }
        }

        MapTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen() {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = { /* 시트 닫기 방지 로직 필요 시 */ },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
//        windowInsets = WindowInsets(0)
    ) {
        MapDetailView(
            sheetState = sheetState,
            onDetentChange = { /* 필요 시 상태 전파 */ }
        )
    }
}