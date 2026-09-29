package org.sparcs.soap.app.features.friends.addFriends

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.features.friends.FriendsListViewModel
import org.sparcs.soap.app.features.friends.FriendsListViewModelProtocol
import org.sparcs.soap.app.features.friends.addFriends.components.AddFriendByCodeDialog
import org.sparcs.soap.app.features.friends.addFriends.components.AnimatedMeshGradient
import org.sparcs.soap.app.features.friends.addFriends.components.IncomingRequestQueue
import org.sparcs.soap.app.features.friends.addFriends.components.MyFriendCodeRow
import org.sparcs.soap.app.features.friends.addFriends.components.NearbyFriendsSection
import org.sparcs.soap.app.features.friends.addFriends.components.NearbyHeader
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import org.sparcs.soap.app.theme.ui.DarkColorScheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.friends.PreviewFriendsListViewModel
import timber.log.Timber

/**
 * Wires Add Friends to its view models. [friendsViewModel] is the Friends
 * list's, so a friend added here shows up there, and an add that fails
 * surfaces its alert on the list after this screen closes.
 */
@Composable
fun AddFriendsRoute(
    friendsViewModel: FriendsListViewModelProtocol,
    onClose: () -> Unit,
    nearbyViewModel: NearbyFriendsViewModel = hiltViewModel(),
    isNearbyEnabled: Boolean = NearbyFriendsViewModel.isFeatureEnabled,
) {
    val context = LocalContext.current
    val nearbyState by nearbyViewModel.viewState.collectAsState()
    val myCode by friendsViewModel.myCode.collectAsState()
    val isMyCodeUnavailable by friendsViewModel.isMyCodeUnavailable.collectAsState()
    val isAddingFriend by friendsViewModel.isAddingFriend.collectAsState()

    if (isNearbyEnabled) {
        NearbyDiscoveryEffect(isScanning = nearbyState.isScanning, run = nearbyViewModel::runDiscovery)
    }

    AddFriendsView(
        isNearbyEnabled = isNearbyEnabled,
        nearbyState = nearbyState,
        myCode = myCode,
        isMyCodeUnavailable = isMyCodeUnavailable,
        isAddingFriend = isAddingFriend,
        onClose = onClose,
        onGrantPermission = nearbyViewModel::grantPermission,
        onOpenSettings = { reason -> context.openSettings(reason) },
        onTapPeer = nearbyViewModel::tap,
        onAcceptPeer = nearbyViewModel::accept,
        onDeclinePeer = nearbyViewModel::decline,
        onAddByCode = { code -> friendsViewModel.addFriend(code, onComplete = onClose) }
    )
}

/**
 * Discovery runs only while the screen is at least STARTED and stops when it
 * goes to the background, as BLE scanning and advertising will need to.
 */
@Composable
private fun NearbyDiscoveryEffect(isScanning: Boolean, run: suspend () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(isScanning, lifecycleOwner) {
        if (!isScanning) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { run() }
    }
}

@Composable
fun AddFriendsView(
    isNearbyEnabled: Boolean,
    nearbyState: NearbyFriendsViewState,
    myCode: String?,
    isMyCodeUnavailable: Boolean,
    isAddingFriend: Boolean,
    onClose: () -> Unit,
    onGrantPermission: () -> Unit,
    onOpenSettings: (NearbyUnavailableReason) -> Unit,
    onTapPeer: (NearbyPeer) -> Unit,
    onAcceptPeer: (NearbyPeer) -> Unit,
    onDeclinePeer: (NearbyPeer) -> Unit,
    onAddByCode: (String) -> Unit,
) {
    // Always dark, like iOS: the content sits on the dark mesh gradient.
    MaterialTheme(colorScheme = DarkColorScheme) {
        LightSystemBarIconsEffect()
        Box(Modifier.fillMaxSize()) {
            AnimatedMeshGradient()
            AddFriendsScaffold(
                isNearbyEnabled = isNearbyEnabled,
                nearbyState = nearbyState,
                myCode = myCode,
                isMyCodeUnavailable = isMyCodeUnavailable,
                isAddingFriend = isAddingFriend,
                onClose = onClose,
                onGrantPermission = onGrantPermission,
                onOpenSettings = onOpenSettings,
                onTapPeer = onTapPeer,
                onAcceptPeer = onAcceptPeer,
                onDeclinePeer = onDeclinePeer,
                onAddByCode = onAddByCode
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFriendsScaffold(
    isNearbyEnabled: Boolean,
    nearbyState: NearbyFriendsViewState,
    myCode: String?,
    isMyCodeUnavailable: Boolean,
    isAddingFriend: Boolean,
    onClose: () -> Unit,
    onGrantPermission: () -> Unit,
    onOpenSettings: (NearbyUnavailableReason) -> Unit,
    onTapPeer: (NearbyPeer) -> Unit,
    onAcceptPeer: (NearbyPeer) -> Unit,
    onDeclinePeer: (NearbyPeer) -> Unit,
    onAddByCode: (String) -> Unit,
) {
    var isCodeDialogPresented by rememberSaveable { mutableStateOf(false) }
    val incomingPeers = if (isNearbyEnabled) nearbyState.incomingPeers else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_friends_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.add_friends_close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = incomingPeers.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    IncomingRequestQueue(
                        peers = incomingPeers,
                        onAccept = onAcceptPeer,
                        onDecline = onDeclinePeer
                    )
                }
                MyFriendCodeRow(code = myCode, isUnavailable = isMyCodeUnavailable)
                Button(
                    onClick = { isCodeDialogPresented = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.add_friends_via_code), modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        // Transparent so the gradient behind shows through every part.
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.analyticsScreen("AddFriends")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 8.dp)
        ) {
            if (isNearbyEnabled) {
                NearbyHeader(
                    isScanning = nearbyState.isScanning,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                NearbyFriendsSection(
                    state = nearbyState,
                    onGrantPermission = onGrantPermission,
                    onOpenSettings = onOpenSettings,
                    onTapPeer = onTapPeer,
                    modifier = Modifier.weight(1f)
                )
            } else {
                CodeOnlyIntro(Modifier.weight(1f))
            }
        }
    }

    if (isCodeDialogPresented) {
        AddFriendByCodeDialog(
            isAdding = isAddingFriend,
            onAdd = onAddByCode,
            onDismiss = { isCodeDialogPresented = false }
        )
    }
}

/** Shown instead of the nearby section while that feature is switched off. */
@Composable
private fun CodeOnlyIntro(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                stringResource(R.string.add_friends_code_only_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.add_friends_code_only_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * The gradient is dark in both themes, so status and navigation bar icons turn
 * light while this screen is shown and go back to what they were afterwards.
 */
@Composable
private fun LightSystemBarIconsEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val wasLightStatusBars = controller.isAppearanceLightStatusBars
        val wasLightNavigationBars = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = wasLightStatusBars
            controller.isAppearanceLightNavigationBars = wasLightNavigationBars
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openSettings(reason: NearbyUnavailableReason) {
    val intent = when (reason) {
        NearbyUnavailableReason.BluetoothOff -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        else -> Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    }
    try {
        startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Timber.w(e, "No activity to open settings for %s", reason)
    }
}

// MARK: - Previews

/** The mock list with the given states applied in order; extra peers stay idle. */
private fun previewPeers(vararg states: NearbyPeerState): List<NearbyPeer> =
    NearbyPeer.mockList().mapIndexed { index, peer ->
        states.getOrNull(index)?.let { peer.copy(state = it) } ?: peer
    }

@Composable
private fun AddFriendsPreview(
    state: NearbyFriendsViewState,
    myCode: String? = "ACD347",
    isMyCodeUnavailable: Boolean = false,
    isNearbyEnabled: Boolean = true,
) {
    Theme {
        AddFriendsView(
            isNearbyEnabled = isNearbyEnabled,
            nearbyState = state,
            myCode = myCode,
            isMyCodeUnavailable = isMyCodeUnavailable,
            isAddingFriend = false,
            onClose = {},
            onGrantPermission = {},
            onOpenSettings = {},
            onTapPeer = {},
            onAcceptPeer = {},
            onDeclinePeer = {},
            onAddByCode = {}
        )
    }
}

/**
 * Starts at the permission prompt; run it in interactive mode and tap Allow to
 * watch people appear and send requests.
 */
@Preview(name = "Live Mock Flow", showBackground = true)
@Composable
private fun LiveMockFlowPreview() {
    Theme {
        AddFriendsRoute(
            friendsViewModel = remember { PreviewFriendsListViewModel(FriendsListViewModel.ViewState.Empty) },
            onClose = {},
            nearbyViewModel = remember {
                NearbyFriendsViewModel(
                    NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
                    simulatesDiscovery = true
                )
            },
            isNearbyEnabled = true
        )
    }
}

@Preview(name = "Permission Required", showBackground = true)
@Composable
private fun PermissionRequiredPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired))

@Preview(name = "Permission Denied", showBackground = true, locale = "ko")
@Composable
private fun PermissionDeniedPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionDenied))

@Preview(name = "Bluetooth Off", showBackground = true)
@Composable
private fun BluetoothOffPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.BluetoothOff))

@Preview(name = "Unsupported Device", showBackground = true)
@Composable
private fun UnsupportedPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.Unsupported))

@Preview(name = "Searching", showBackground = true)
@Composable
private fun SearchingPreview() = AddFriendsPreview(NearbyFriendsViewState.Scanning())

@Preview(name = "People Found", showBackground = true)
@Composable
private fun PeopleFoundPreview() = AddFriendsPreview(NearbyFriendsViewState.Scanning(previewPeers().take(3)))

@Preview(name = "Incoming Request", showBackground = true)
@Composable
private fun IncomingRequestPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Scanning(previewPeers(NearbyPeerState.Idle, NearbyPeerState.Incoming)))

@Preview(name = "Multiple Requests", showBackground = true, locale = "ko")
@Composable
private fun MultipleRequestsPreview() = AddFriendsPreview(
    NearbyFriendsViewState.Scanning(
        previewPeers(
            NearbyPeerState.Incoming,
            NearbyPeerState.Incoming,
            NearbyPeerState.Idle,
            NearbyPeerState.Incoming,
            NearbyPeerState.Incoming
        )
    )
)

@Preview(name = "Every State", showBackground = true)
@Composable
private fun EveryStatePreview() = AddFriendsPreview(
    NearbyFriendsViewState.Scanning(
        previewPeers(
            NearbyPeerState.Idle,
            NearbyPeerState.Requested,
            NearbyPeerState.Adding,
            NearbyPeerState.Added,
            NearbyPeerState.Failed
        )
    )
)

@Preview(name = "Code Unavailable", showBackground = true)
@Composable
private fun CodeUnavailablePreview() =
    AddFriendsPreview(NearbyFriendsViewState.Scanning(), myCode = null, isMyCodeUnavailable = true)

@Preview(name = "Nearby Disabled (Release)", showBackground = true)
@Composable
private fun NearbyDisabledPreview() =
    AddFriendsPreview(NearbyFriendsViewState.Scanning(), isNearbyEnabled = false)
