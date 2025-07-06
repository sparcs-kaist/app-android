package com.example.soap.Features.TaxiPreview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Features.TaxiPreview.Components.RouteHeaderView
import com.example.soap.Features.TaxiPreview.Components.TaxiInfoItem
import com.example.soap.Features.TaxiPreview.Components.TaxiInfoSection
import com.example.soap.Models.RoomInfo
import com.example.soap.R
import com.example.soap.Utilities.Extensions.DateString
import com.example.soap.Utilities.Extensions.TimeString
import com.example.soap.Utilities.Mocks.mock
import com.example.soap.ui.theme.SoapTheme

@Composable
fun TaxiPreviewView(
    roomInfo: RoomInfo,
){
    Column{
        //map
        Column(horizontalAlignment = Alignment.Start){
            RouteHeaderView(
                origin = roomInfo.origin,
                destination = roomInfo.destination
            )

            TaxiInfoSection(
                items = listOf(
                    TaxiInfoItem.plain(
                        label = "Departure Date",
                        value = roomInfo.departureTime.DateString.toString()
                    ),
                    TaxiInfoItem.plain(
                        label = "Departure Time",
                        value = roomInfo.departureTime.TimeString.toString()
                    )
                )
            )

            TaxiInfoSection(
                items = listOf(
                    TaxiInfoItem.plain(
                    label = "Room Name",
                    value = roomInfo.name
                    ),
                    TaxiInfoItem.withIcon(
                        label = "Participants",
                        value = "${roomInfo.occupancy}/${roomInfo.capacity}",
                        systemImage = R.drawable.arrow_forward_ios
                    )
                )
            )

            Spacer(Modifier.padding(8.dp))

            Row {
                //share과 join 버튼
            }
        }

    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme{ TaxiPreviewView(roomInfo = RoomInfo.mock()) }
}