package org.sparcs.soap.app.features.taxiChatList.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiChatRoomList(
    onGoing: List<TaxiRoom>,
    done: List<TaxiRoom>,
    taxiUser: TaxiUser?,
    onRoomClick: (TaxiRoom) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier.fillMaxSize()
    ) {
        roomSection(R.string.active_groups, R.string.no_active_groups, onGoing, "ongoing", taxiUser, onRoomClick)
        roomSection(R.string.past_groups, R.string.no_past_groups, done, "done", taxiUser, onRoomClick)
    }
}

/** A section header, then the rooms as cards, or an empty state. */
private fun LazyListScope.roomSection(
    @StringRes title: Int,
    @StringRes empty: Int,
    rooms: List<TaxiRoom>,
    key: String,
    taxiUser: TaxiUser?,
    onRoomClick: (TaxiRoom) -> Unit,
) {
    item(key = "$key-header") { RoomSectionHeader(stringResource(title)) }
    if (rooms.isEmpty()) {
        item(key = "$key-empty") {
            UnavailableView(
                icon = Icons.Outlined.ErrorOutline,
                title = stringResource(R.string.no_result),
                description = stringResource(empty)
            )
        }
    }
    items(rooms, key = { room -> "$key-${room.id}" }) { room ->
        Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            TaxiRoomCell(room = room, taxiUser = taxiUser, showsRiders = true, onClick = { onRoomClick(room) })
        }
    }
}

@Composable
private fun RoomSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .semantics { heading() }
    )
}

@Composable
fun TaxiChatRoomListSkeleton(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    LazyColumn(
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        item { RoomSectionHeader(stringResource(R.string.active_groups)) }
        items(4) { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { TaxiRoomSkeletonCell() } }
        item { RoomSectionHeader(stringResource(R.string.past_groups)) }
        items(2) { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { TaxiRoomSkeletonCell() } }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaxiChatRoomListPreview() {
    Theme {
        TaxiChatRoomList(
            onGoing = TaxiRoom.mockList().take(3),
            done = TaxiRoom.mockList().takeLast(2),
            taxiUser = TaxiUser.mock(),
            onRoomClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaxiChatRoomListSkeletonPreview() {
    Theme {
        TaxiChatRoomListSkeleton()
    }
}
