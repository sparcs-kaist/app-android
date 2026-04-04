package org.sparcs.soap.App.Features.Search.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.TaxiRoomCell
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun <T : Any> SearchContent(
    results: List<T>,
    loadMoreThreshold: Double = 0.8,
    onLoadMore: ( () -> Unit)? = null,
    cell: @Composable (T) -> Unit,
) {
    var isLoadingMore by remember { mutableStateOf(false) }

    if (results.isEmpty()) {
        UnavailableView(
            icon = Icons.Rounded.ReportProblem,
            title = stringResource(R.string.no_results),
            description = stringResource(R.string.no_results),
        )
    } else {
        Column {
            results.forEachIndexed { index, item ->
                Column {
                    cell(item)
                    if (index != results.lastIndex) {
                        HorizontalDivider()
                    }
                }

                val thresholdIndex = (results.size * loadMoreThreshold).toInt()
                if (index >= thresholdIndex && onLoadMore != null && !isLoadingMore) {
                    LaunchedEffect(key1 = index) {
                        isLoadingMore = true
                        onLoadMore()
                        isLoadingMore = false
                    }
                }
            }

            if (isLoadingMore) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        SearchContent(
            results = TaxiRoom.mockList(),
            cell = {
                TaxiRoomCell(
                    room = it,
                    onClick = {}
                )
            }
        )
    }
}