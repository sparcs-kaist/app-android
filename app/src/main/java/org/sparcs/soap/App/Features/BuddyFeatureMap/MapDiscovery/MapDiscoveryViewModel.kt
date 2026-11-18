package org.sparcs.soap.App.Features.BuddyFeatureMap.MapDiscovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.sparcs.soap.App.Domain.Models.Map.CampusLocation
import org.sparcs.soap.App.Shared.Mocks.Map.mockList
import javax.inject.Inject

interface MapDiscoveryViewModelProtocol{

}

@HiltViewModel
class MapDiscoveryViewModel @Inject constructor(
) : ViewModel(), MapDiscoveryViewModelProtocol {

    var locations by mutableStateOf(CampusLocation.mockList())
    var selectedLocation by mutableStateOf<CampusLocation?>(null)

}