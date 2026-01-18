package org.sparcs.App.Shared.Views.ContentViews

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R


@Composable
fun ErrorView(icon: ImageVector = Icons.Default.Warning, message: String, onRetry: () -> Unit) {
    val context = LocalContext.current
    val networkErrorText = context.getString(R.string.network_connection_error)

    val isNetworkError = message == networkErrorText ||
                message.contains("host", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true)

    val iconPainter = if (isNetworkError) {
        painterResource(id = R.drawable.outline_wifi_off_24)
    } else {
        rememberVectorPainter(image = icon)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = "Error",
            modifier = Modifier.size(if (isNetworkError) 80.dp else 48.dp),
            tint = if (isNetworkError) MaterialTheme.colorScheme.grayBB else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isNetworkError)
                stringResource(R.string.network_connection_error)
            else
                message,
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
            message = "Error",
            onRetry = {}
        )
    }
}