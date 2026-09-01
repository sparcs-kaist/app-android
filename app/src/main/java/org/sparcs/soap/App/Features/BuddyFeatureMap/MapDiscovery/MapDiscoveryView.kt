package org.sparcs.soap.App.Features.BuddyFeatureMap.MapDiscovery

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.sparcs.soap.App.Features.BuddyFeatureMap.Map.MapView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MapDiscoveryView(
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: MapDiscoveryViewModel = viewModel(),
) {
    var showMap by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Discovery") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(280.dp)
                    .fillMaxWidth()
                    .sharedElement(
                        rememberSharedContentState(key = "MapExplorerViewSource"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .clickable { showMap = true },
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapView(
                        locations = viewModel.locations,
                        selectedLocation = viewModel.selectedLocation,
                        onLocationSelected = { viewModel.selectedLocation = it }
                    )
                }
            }
        }
    }

    if (showMap) {

    }
}