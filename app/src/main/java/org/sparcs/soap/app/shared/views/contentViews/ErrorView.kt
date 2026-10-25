package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.isNetworkError
import org.sparcs.soap.app.theme.ui.Theme


@Composable
fun ErrorView(
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    error: Exception,
    defaultMessageResId: Int? = null,
    onRetry: () -> Unit,
) {
    val isNetworkError = error.isNetworkError()

    val displayIcon = if (isNetworkError) Icons.Outlined.WifiOff else icon
    val iconSize = if (isNetworkError) 60.dp else 48.dp
    val iconTint =
        if (isNetworkError) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error

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
            style = MaterialTheme.typography.titleMedium
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
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.error_try_again))
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        ErrorView(
            error = Exception(),
            onRetry = {}
        )
    }
}