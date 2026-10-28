package org.sparcs.soap.app.features.post.components

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** Share pill that opens the system share sheet with [url]. */
@Composable
fun PostShareButton(url: String, context: Context) {
    ActionPillIconButton(
        imageVector = Icons.Outlined.Share,
        contentDescription = stringResource(R.string.share),
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        }
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { PostShareButton("", LocalContext.current) }
}
