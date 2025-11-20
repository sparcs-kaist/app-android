package com.sparcs.soap.Features.Home.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.LocalizedText
import com.sparcs.soap.Shared.Mocks.mockList
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.darkGray
import com.sparcs.soap.ui.theme.grayBB

@Composable
fun TaxiRecentSection(roomList: List<TaxiRoom>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clickable { }
        ) {
            Text(
                text = stringResource(R.string.taxi),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = "Go to Taxi Board",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.darkGray
            )
        }
    }

    LazyRow(
        contentPadding = PaddingValues(end = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(roomList) { room ->

            Spacer(Modifier.padding(horizontal = 8.dp))

            Card(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(screenWidth - 60.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_near_me),
                                    contentDescription = null,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                LocalizedText(
                                    text = room.source.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.padding(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrival_point),
                                    contentDescription = null,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                LocalizedText(
                                    text = room.destination.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.Top)
                        ) {
                            Text(
                                text = "${room.participants.size}/${room.capacity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50)
                            )
                            Icon(
                                painter = painterResource(R.drawable.group),
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "${room.departAt}\tLorem ipsum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.grayBB,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
@Preview
private fun Preview(){
    Theme { TaxiRecentSection(TaxiRoom.mockList()) }
}