package org.sparcs.soap.app.features.taxiChatList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomSkeletonCell

@Composable
fun TaxiChatRoomList(
    onGoing: List<TaxiRoom>,
    done: List<TaxiRoom>,
    taxiUser: TaxiUser?,
    onRoomClick: (TaxiRoom) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.active_groups),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            if (onGoing.isEmpty()) {
                UnavailableView(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.no_result),
                    description = stringResource(R.string.no_active_groups)
                )
            }
        }

        items(onGoing) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) },
                taxiUser = taxiUser
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.past_groups),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            if (done.isEmpty()) {
                UnavailableView(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.no_result),
                    description = stringResource(R.string.no_past_groups)
                )
            }
        }

        items(done) { room ->
            TaxiRoomCell(
                room = room,
                onClick = { onRoomClick(room) },
                taxiUser = taxiUser
            )
        }
    }
}

@Composable
fun TaxiChatRoomListSkeleton() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.active_groups),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        repeat(4) {
            item {
                TaxiRoomSkeletonCell()
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.past_groups),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        repeat(2) {
            item {
                TaxiRoomSkeletonCell()
            }
        }
    }
}
