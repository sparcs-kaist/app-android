package org.sparcs.soap.app.shared.views.taxiRoomCell

import android.text.format.DateFormat
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRouteStops
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.isDepartingSoon
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.rememberCurrentMinute
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiCountdown
import org.sparcs.soap.app.theme.ui.creditCompleteColor
import org.sparcs.soap.app.theme.ui.creditIncompleteColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRoomStatusIndicator
import org.sparcs.soap.app.theme.ui.Theme

/** Narrower than this inside the card's padding — a column beside a chat on a wide display — the time goes above the route rather than beside it. */
private val SideBySideMinWidth = 280.dp

/** Wide enough for the time in any locale's format, so the dividers line up down the list. */
private val TimeColumnWidth = 88.dp

/**
 * A room as a card, led by when it leaves: the time, with a countdown when it is close and the
 * date otherwise, beside the route — or above it in a narrow column — then the title and one dot
 * per seat, or, once the ride has left, where your settlement stands.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaxiRoomCell(
    room: TaxiRoom,
    taxiUser: TaxiUser? = null,
    onClick: () -> Unit = {},
) {
    val now by rememberCurrentMinute()
    val context = LocalContext.current
    val time = remember(room.departAt) { DateFormat.getTimeFormat(context).format(room.departAt) }
    val date = remember(room.departAt) { room.departAt.shortDayString() }
    val countdown = if (room.isDeparted) null else taxiCountdown(room.departAt, now)
    val detail = countdown ?: date
    val detailColor = when {
        countdown != null && isDepartingSoon(room.departAt, now) -> creditIncompleteColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            val departure: @Composable () -> Unit = {
                Text(
                    time,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = MaterialTheme.typography.titleLargeEmphasized.fontSize),
                )
            }
            val departureDetail: @Composable () -> Unit = {
                Text(
                    detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = detailColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (maxWidth >= SideBySideMinWidth) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(IntrinsicSize.Min),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        modifier = Modifier
                            .width(TimeColumnWidth)
                            .fillMaxHeight(),
                    ) {
                        departure()
                        departureDetail()
                    }
                    VerticalDivider(Modifier.padding(vertical = 2.dp))
                    RoomDetails(room, taxiUser, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        departure()
                        Box(Modifier.padding(bottom = 3.dp)) { departureDetail() }
                    }
                    RoomDetails(room, taxiUser)
                }
            }
        }
    }
}

/** The route, then the title with the seats, or your settlement once the ride has left. */
@Composable
private fun RoomDetails(room: TaxiRoom, taxiUser: TaxiUser?, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
        TaxiRouteStops(
            source = room.source.title.localized(),
            destination = room.destination.title.localized(),
            style = MaterialTheme.typography.titleMedium,
            markerSize = 16.dp,
            rowSpacing = 4.dp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                room.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            if (room.isDeparted) {
                TaxiRoomStatusIndicator(
                    settlementType = room.participants.firstOrNull { it.id == taxiUser?.oid }?.isSettlement
                        ?: TaxiParticipant.SettlementType.NotDeparted,
                    settlementCount = room.settlementTotal ?: 0,
                    participantsCount = room.participants.size
                )
            } else {
                TaxiSeatDots(taken = room.participants.size, capacity = room.capacity)
            }
        }
    }
}

/** One dot per seat, filled for each rider and coloured by how many seats are left. */
@Composable
private fun TaxiSeatDots(taken: Int, capacity: Int) {
    val filled = when (capacity - taken) {
        in Int.MIN_VALUE..0 -> MaterialTheme.colorScheme.error
        1 -> creditIncompleteColor
        else -> creditCompleteColor
    }
    val empty = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = "$taken/$capacity" },
    ) {
        repeat(capacity) { index ->
            Box(
                Modifier
                    .size(8.dp)
                    .background(if (index < taken) filled else empty, CircleShape)
            )
        }
    }
}

/** "Mon, Oct 26": the date in the user's locale, short enough for the time column. */
private fun Date.shortDayString(): String {
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdEEE")
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

/** Placeholder with the same layout and text styles as [TaxiRoomCell]. */
@Composable
fun TaxiRoomSkeletonCell() {
    val typography = MaterialTheme.typography
    val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Card(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(IntrinsicSize.Min),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .width(TimeColumnWidth)
                    .fillMaxHeight(),
            ) {
                SkeletonLine(typography.titleLarge, Modifier.fillMaxWidth(0.8f))
                SkeletonLine(typography.labelMedium, Modifier.fillMaxWidth(0.6f))
            }
            VerticalDivider(Modifier.padding(vertical = 2.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { line ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).background(tint, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            SkeletonLine(typography.titleMedium, Modifier.fillMaxWidth(if (line == 0) 0.55f else 0.7f))
                        }
                    }
                }
                SkeletonLine(typography.bodySmall, Modifier.fillMaxWidth(0.5f))
            }
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.adaptiveIconSize(MaterialTheme.typography.bodyLarge),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaxiRoomCell(room = TaxiRoom.mockList()[0], taxiUser = TaxiUser.mock(), onClick = {})
            TaxiRoomSkeletonCell()
        }
    }
}