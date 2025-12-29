package org.sparcs.App.Features.Settings.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB

@Composable
fun RowElementView(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Spacer(Modifier.padding(horizontal = 16.dp))
        Text(
            text = content,
            color = MaterialTheme.colorScheme.grayBB
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        LazyColumn {
            item {
                RowElementView(title = "Title", content = "Content")
            }
        }
    }
}
