package org.sparcs.soap.App.Shared.Views.ContentViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R


@Composable
fun ErrorView(
    icon: ImageVector = Icons.Default.Warning,
    error: Exception,
    defaultMessageResId: Int? = null,
    onRetry: () -> Unit,
) {
    val isNetworkError = error.isNetworkError()

    val displayIcon = if (isNetworkError) Icons.Rounded.WifiOff else icon
    val iconSize = if (isNetworkError) 60.dp else 48.dp
    val iconTint =
        if (isNetworkError) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error

    val errorMessage = when {
        isNetworkError -> stringResource(R.string.network_connection_error)
        defaultMessageResId != null -> stringResource(defaultMessageResId)
        else -> error.localizedMessage ?: stringResource(R.string.something_went_wrong)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = displayIcon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRetry() },
            modifier = Modifier.fillMaxWidth(0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.error_try_again))
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        ErrorView(
            icon = Icons.Default.Warning,
            error = Exception(),
            onRetry = {}
        )
    }
}