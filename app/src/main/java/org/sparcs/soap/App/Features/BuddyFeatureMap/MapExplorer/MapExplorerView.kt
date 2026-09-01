package org.sparcs.soap.App.Features.BuddyFeatureMap.MapExplorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Map.CampusLocation
import org.sparcs.soap.App.Features.BuddyFeatureMap.Map.MapView
import org.sparcs.soap.App.Features.BuddyFeatureMap.MapDetail.MapDetailView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapExplorerView(
    locations: List<CampusLocation>,
    selectedLocation: CampusLocation?,
    onLocationSelected: (CampusLocation?) -> Unit,
    onDismiss: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 80.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetSwipeEnabled = true,
        sheetContent = {
            MapDetailView(
                sheetState = scaffoldState.bottomSheetState,
                onDetentChange = { /* 높이 변경 콜백 */ }
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 현재 위치 로직 */ }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MapView(
                locations = locations,
                selectedLocation = selectedLocation,
                onLocationSelected = onLocationSelected,
                modifier = Modifier.fillMaxSize()
            )

        }
    }
}