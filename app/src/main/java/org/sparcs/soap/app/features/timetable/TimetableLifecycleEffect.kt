package org.sparcs.soap.app.features.timetable

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

@Composable
internal fun TimetableLifecycleEffect(viewModel: TimetableViewModelProtocol) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    DisposableEffect(viewModel, owner, context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fun updateConnectivity() {
            val network = manager.activeNetwork
            val connected = if (network == null) false else {
                val capabilities = manager.getNetworkCapabilities(network) ?: return
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> true
                    !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> false
                    else -> return
                }
            }
            scope.launch { viewModel.connectivityChanged(connected) }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = updateConnectivity()
            override fun onLost(network: Network) = updateConnectivity()
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) = updateConnectivity()
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateConnectivity()
                viewModel.fetchData()
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        owner.lifecycle.addObserver(observer)
        updateConnectivity()
        onDispose {
            manager.unregisterNetworkCallback(callback)
            owner.lifecycle.removeObserver(observer)
        }
    }
}
