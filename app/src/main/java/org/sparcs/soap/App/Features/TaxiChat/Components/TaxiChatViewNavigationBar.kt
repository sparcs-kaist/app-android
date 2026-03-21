package org.sparcs.soap.App.Features.TaxiChat.Components

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Shared.Extensions.formattedString
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom,
    onDismiss: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onReport: () -> Unit,
    onClickLeave: () -> Unit,
    isEnabled: Boolean
) {
    val context = LocalContext.current
    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )

    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(text = room.title, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = "${room.source.title} → ${room.destination.title}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        actions = {
            TaxiChatViewDropDownMenu(
                room = room,
                onClickShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                onClickCallTaxi = { onClickCallTaxi() },
                onClickReport = { onReport() },
                onClickLeave = { onClickLeave() },
                isEnabled = isEnabled
            ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}



@Composable
@Preview
private fun Preview(){
    Theme {
        Box(Modifier.fillMaxSize()){ TaxiChatViewNavigationBar(TaxiRoom.mock(), {}, {}, {}, {}, true) } }
}

