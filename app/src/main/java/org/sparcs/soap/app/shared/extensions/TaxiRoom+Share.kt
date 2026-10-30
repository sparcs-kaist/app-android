package org.sparcs.soap.app.shared.extensions

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom

@Composable
fun rememberTaxiRoomShare(room: TaxiRoom): () -> Unit {
    val context = LocalContext.current
    val message = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        "${Constants.TAXI_INVITE_URL}${room.id}"
    )
    return {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(send, null))
    }
}
