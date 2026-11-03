package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun UpdatedMarker(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.post_updated)
    Box(
        modifier
            .size(7.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .semantics { contentDescription = description }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        UpdatedMarker(Modifier.padding(16.dp))
    }
}
