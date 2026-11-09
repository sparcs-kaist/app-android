package org.sparcs.soap.app.features.taxiPreview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import org.sparcs.soap.app.features.taxiPreview.components.TaxiDepartureHeader
import org.sparcs.soap.app.features.taxiPreview.components.TaxiRideStats
import org.sparcs.soap.app.features.taxiPreview.components.TaxiRiderRow
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRouteStops
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.createKakaoMap
import org.sparcs.soap.app.shared.extensions.TaxiRouteStyle
import org.sparcs.soap.app.shared.extensions.drawTaxiRoute
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.extensions.setSafeArea
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.theme_light_onPrimary
import org.sparcs.soap.app.theme.ui.theme_light_onSurface
import org.sparcs.soap.app.theme.ui.theme_light_onTertiary
import org.sparcs.soap.app.theme.ui.theme_light_primary
import org.sparcs.soap.app.theme.ui.theme_light_surfaceContainerHighest
import org.sparcs.soap.app.theme.ui.theme_light_tertiary
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiPreviewViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaxiPreviewView(
    room: TaxiRoom,
    viewModel: TaxiPreviewViewModelProtocol = hiltViewModel<TaxiPreviewViewModel>(),
    onDismiss: () -> Unit,
    navController: NavController,
    isOpen: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Kakao's map view is slow to create and blocks the main thread while it starts, which would
    // drop every frame of the sheet's opening animation; so it is made once the sheet holding the
    // preview has opened ([isOpen]), and kept from then on.
    var showsMap by remember { mutableStateOf(isOpen) }
    LaunchedEffect(isOpen) { if (isOpen) showsMap = true }

    val isPreview = LocalInspectionMode.current
    var isMapLoading by remember { mutableStateOf(true) }
    var isMapError by remember { mutableStateOf(false) }

    val blockStatus = viewModel.blockStatus.collectAsState().value
    val taxiUser = viewModel.taxiUser.collectAsState().value
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    // Null until the route arrives; then its points, and its figures when one was found.
    var route by remember { mutableStateOf<TaxiRoute?>(null) }

    // The route in the theme's primary, its stops as Material 3 Expressive shapes: the meeting
    // point in primary, the destination in tertiary, each named in a pill like the sheet's chips.
    // Always from the light scheme, whose tones are made for a light background, as Kakao's map is
    // in either appearance.
    val routeStyle = TaxiRouteStyle(
        routeColor = theme_light_primary.toArgb(),
        // Edged like the markers' rings, which it runs into.
        routeOutlineColor = theme_light_surfaceContainerHighest.toArgb(),
        start = TaxiRouteStyle.Stop(
            title = room.source.title.localized(),
            shape = MaterialShapes.Cookie9Sided,
            containerColor = theme_light_primary.toArgb(),
            contentColor = theme_light_onPrimary.toArgb(),
            glyph = R.drawable.round_near_me,
        ),
        end = TaxiRouteStyle.Stop(
            title = room.destination.title.localized(),
            shape = MaterialShapes.Clover4Leaf,
            containerColor = theme_light_tertiary.toArgb(),
            contentColor = theme_light_onTertiary.toArgb(),
            glyph = R.drawable.round_location_on,
        ),
        labelContainerColor = theme_light_surfaceContainerHighest.toArgb(),
        labelContentColor = theme_light_onSurface.toArgb(),
    )
    // The bottom sheet's own colour, so the details and the map's fade run on from its drag handle.
    val sheetColor = BottomSheetDefaults.ContainerColor

    val isJoinButtonDisabled: Boolean =
        taxiUser == null || (!viewModel.isJoined(room.participants) && (room.participants.size >= room.capacity ||
                room.isDeparted ||
                blockStatus != TaxiRoomBlockStatus.Allow))

    val shareUrl = "${Constants.TAXI_INVITE_URL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )
    // A phone in landscape is too short to stack the map above the details, so they go side by
    // side; a tablet is tall enough in either orientation to stack them.
    val isShort = LocalConfiguration.current.screenHeightDp < 480

    // The map's edges that are covered: the fade into the details (below it, or beside it on a
    // short screen) and the rounded corners; and around each stop, its marker, which is centred on
    // it with its name in a pill beneath, as wide as the name.
    val density = LocalDensity.current
    val fitPadding = with(density) { 16.dp.roundToPx() }
    val applySafeArea: (KakaoMap) -> Unit = { map ->
        with(density) {
            val fade = 64.dp.roundToPx()
            val edge = 16.dp.roundToPx()
            val markerSide = 16.dp.roundToPx()
            // Below the badge, the pill may run onto the fade's faint first half.
            val markerBelow = 20.dp.roundToPx()
            val right = if (isShort) fade else edge
            val bottom = if (isShort) edge else fade
            map.setSafeArea(
                left = edge + markerSide,
                top = edge,
                right = right + markerSide,
                bottom = bottom + markerBelow,
                logoRight = right,
                logoBottom = bottom,
            )
        }
    }

    LaunchedEffect(room) {
        route = viewModel.calculateRoute(
            LatLng.from(room.source.latitude, room.source.longitude),
            LatLng.from(room.destination.latitude, room.destination.longitude)
        )
    }

    LaunchedEffect(kakaoMap, route) {
        val map = kakaoMap ?: return@LaunchedEffect
        applySafeArea(map)
        val pathPoints = route?.points ?: return@LaunchedEffect

        map.drawTaxiRoute(
            context = context,
            startPos = LatLng.from(room.source.latitude, room.source.longitude),
            endPos = LatLng.from(room.destination.latitude, room.destination.longitude),
            pathPoints = pathPoints,
            style = routeStyle,
            fitPadding = fitPadding,
        )
        isMapLoading = false
    }

    val routeMap: @Composable (Modifier) -> Unit = { modifier ->
        Box(
            modifier = modifier
                .drawWithCache {
                    val radius = CornerRadius(28.dp.toPx())
                    val bounds = Rect(0f, 0f, size.width, size.height)
                    val corners = Path.combine(
                        PathOperation.Difference,
                        Path().apply { addRect(bounds) },
                        Path().apply {
                            // Beside the details on a short screen, only the sheet's top-left corner is the map's.
                            addRoundRect(
                                RoundRect(bounds, topLeft = radius, topRight = if (isShort) CornerRadius.Zero else radius)
                            )
                        }
                    )
                    // The map fades into the sheet, so the details below it, or beside it on a
                    // short screen, read as one panel with it.
                    val fadeLength = 64.dp.toPx()
                    val colors = listOf(sheetColor.copy(alpha = 0f), sheetColor)
                    val fade = if (isShort) {
                        Brush.horizontalGradient(colors, startX = size.width - fadeLength, endX = size.width)
                    } else {
                        Brush.verticalGradient(colors, startY = size.height - fadeLength, endY = size.height)
                    }
                    val fadeOrigin = if (isShort) Offset(size.width - fadeLength, 0f) else Offset(0f, size.height - fadeLength)
                    onDrawWithContent {
                        drawContent()
                        drawRect(fade, topLeft = fadeOrigin)
                        drawPath(corners, sheetColor)
                    }
                }
        ) {
            //MAP
            if (!isPreview && !isMapError && !showsMap) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            } else if (!isPreview && !isMapError) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        ctx.createKakaoMap(
                            room,
                            onMapReady = {
                                applySafeArea(it)
                                kakaoMap = it
                            },
                            fitPadding = fitPadding,
                            onError = {
                                isMapError = true
                                isMapLoading = false
                            }
                        )
                    }
                )
                if (isMapLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = {}, onTap = {}, onPress = {})
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                )
            }
        }

    }

    val details: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            // At the bottom beside the map on a short screen, as below it otherwise, so Join stays
            // where your thumb is.
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            modifier = modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        ) {
            TaxiDepartureHeader(room.departAt)

            TaxiRouteStops(
                source = room.source.title.localized(),
                destination = room.destination.title.localized(),
            )

            // Held from the start, so the rows below don't move when the route arrives; gone only
            // when no route could be found.
            if (route?.hasStats != false) {
                TaxiRideStats(departAt = room.departAt, route = route)
            }

            TaxiRiderRow(participants = room.participants, capacity = room.capacity)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val isAlreadyJoined = viewModel.isJoined(room.participants)

                            if (isAlreadyJoined) {
                                val json = Uri.encode(Gson().toJson(room))
                                navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                                onDismiss()
                            } else {
                                viewModel.joinRoom(id = room.id) {
                                    onDismiss()
                                    val json = Uri.encode(Gson().toJson(room))
                                    navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                                }
                            }
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(ButtonDefaults.MediumContainerHeight),
                    contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                    // The light scheme's vibrant primary in either appearance, like the route on the map.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme_light_primary,
                        contentColor = theme_light_onPrimary,
                    ),
                    enabled = !isJoinButtonDisabled
                ) {
                    Icon(
                        Icons.Outlined.LocalTaxi,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
                    )
                    Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
                    Text(
                        text =
                            when {
                                viewModel.isJoined(room.participants) -> stringResource(R.string.joined_enter_chat)
                                room.participants.size >= room.capacity -> stringResource(R.string.room_full)
                                room.isDeparted -> stringResource(R.string.already_departed)
                                blockStatus == TaxiRoomBlockStatus.TooManyRooms -> stringResource(R.string.room_limit_reached)
                                blockStatus == TaxiRoomBlockStatus.NotPaid -> stringResource(R.string.room_settlement_required)
                                else -> stringResource(R.string.join)
                            },
                        style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    if (isShort) {
        // Sized from the screen rather than from the sheet's constraints, which change over the
        // first frames as the window insets arrive: Kakao's map view blocks the main thread while
        // its surface is resized until it draws a frame, and one that can't draw — refused its key,
        // say — never does, freezing the app.
        val configuration = LocalConfiguration.current
        // Clear of the status bar, the drag handle and the navigation bar.
        val rowHeight = (configuration.screenHeightDp - 160).coerceIn(220, 360).dp
        val detailsScroll = rememberScrollState()
        // Opens on Join rather than on the date when the details have to scroll.
        LaunchedEffect(detailsScroll.maxValue) { detailsScroll.scrollTo(detailsScroll.maxValue) }
        val detailsWidth = (minOf(configuration.screenWidthDp, 640) * 0.45f).dp.coerceIn(320.dp, 400.dp)
        Row(
            Modifier
                .background(sheetColor)
                // Outside the fixed height, and only below, so the insets never resize the map.
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .fillMaxWidth()
                .height(rowHeight)
                .analyticsScreen("Taxi Preview")
        ) {
            routeMap(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            // Scrolls only when the details are taller than the row; otherwise as tall, so they
            // sit at the bottom.
            Column(
                Modifier
                    .width(detailsWidth)
                    .verticalScroll(detailsScroll)
            ) {
                details(Modifier.heightIn(min = rowHeight).padding(top = 16.dp))
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(sheetColor)
                .analyticsScreen("Taxi Preview")
        ) {
            routeMap(
                Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
            details(Modifier)
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

//______________________________________________________

@Preview
@Composable
private fun Preview() {
    Theme {
        TaxiPreviewView(
            room = TaxiRoom.mockList()[1],
            viewModel = PreviewTaxiPreviewViewModel(),
            onDismiss = {},
            navController = rememberNavController()
        )
    }
}
