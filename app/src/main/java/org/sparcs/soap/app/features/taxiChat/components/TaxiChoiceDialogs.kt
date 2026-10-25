package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TaxiDeepLinkHelper
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.extensions.openUri

/** Which app to call the taxi in, as a Material simple dialog. */
@Composable
fun CallTaxiDialog(room: TaxiRoom, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ChoiceDialog(
        title = stringResource(R.string.call_taxi),
        message = stringResource(R.string.taxi_launch_info, room.source.title, room.destination.title),
        onDismiss = onDismiss,
        choices = listOf(
            Choice(stringResource(R.string.open_kakao_t), Icons.Outlined.LocalTaxi) {
                context.openUri(TaxiDeepLinkHelper.getKakaoTUri(room.source, room.destination), "com.kakao.taxi")
            },
            Choice(stringResource(R.string.open_uber), Icons.Outlined.LocalTaxi) {
                context.openUri(TaxiDeepLinkHelper.getUberUri(room.source, room.destination), "com.ubercab")
            },
        )
    )
}

/** How to send the payment, or mark it as already sent. */
@Composable
fun SendPaymentDialog(account: String?, onAlreadySent: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ChoiceDialog(
        title = stringResource(R.string.send_payment),
        message = stringResource(R.string.payment_send_instructions),
        onDismiss = onDismiss,
        choices = listOf(
            Choice(stringResource(R.string.open_kakao_pay), Icons.Outlined.AccountBalanceWallet) {
                context.openUri(TaxiDeepLinkHelper.getKakaoPayUri(context, account), "com.kakao.talk")
            },
            Choice(stringResource(R.string.open_toss), Icons.Outlined.AccountBalanceWallet) {
                context.openUri(TaxiDeepLinkHelper.getTossUri(account), "viva.republica.toss")
            },
            Choice(stringResource(R.string.already_sent), Icons.Outlined.CheckCircle, onAlreadySent),
        )
    )
}

private class Choice(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun ChoiceDialog(title: String, message: String, choices: List<Choice>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message, modifier = Modifier.padding(bottom = 8.dp))
                choices.forEach { choice ->
                    ListItem(
                        headlineContent = { Text(choice.label) },
                        leadingContent = { Icon(choice.icon, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            choice.onClick()
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
