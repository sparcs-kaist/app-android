package org.sparcs.soap.app.shared.sharing

import android.graphics.Bitmap
import android.net.Uri
import android.provider.Telephony
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    content: ShareContent,
    capture: suspend () -> Bitmap,
    onDismiss: () -> Unit,
    onFeed: ((Uri, String) -> Unit)? = null,
    preview: @Composable () -> Unit = {},
    viewModel: ShareViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val launcher = remember(context) { ShareLauncher(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    DisposableEffect(viewModel) { onDispose { viewModel.reset() } }
    LaunchedEffect(state.request) {
        val request = state.request ?: return@LaunchedEffect
        if (request.target == ShareTarget.Feed) sheetState.hide()
        val launched = launcher.launch(request, onFeed)
        if (!launched) {
            Toast.makeText(context, R.string.share_failed, Toast.LENGTH_SHORT).show()
        } else if (request.target != ShareTarget.Copy) {
            if (request.target != ShareTarget.Feed) sheetState.hide()
            onDismiss()
        }
        viewModel.consumeRequest()
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            scope.launch { sheetState.hide(); onDismiss() }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ShareSheetContent(content, state, onFeed != null, { viewModel.prepare(it, content, capture) }, preview)
    }
}

@Composable
internal fun ShareSheetContent(
    content: ShareContent,
    state: ShareState,
    hasFeed: Boolean,
    onTarget: (ShareTarget) -> Unit,
    preview: @Composable () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(content.title, Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        preview()
        if (state.preparing) Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.share_preparing))
        }
        if (state.failed) Text(stringResource(R.string.share_image_failed), Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.error)
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOfNotNull(ShareTarget.Instagram, ShareTarget.Feed.takeIf { hasFeed }, ShareTarget.Messages)) { target ->
                ShareTargetItem(target, !state.preparing) { onTarget(target) }
            }
        }
        Column {
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            listOf(ShareTarget.Copy, ShareTarget.More).forEach { target ->
                Row(
                    Modifier.fillMaxWidth().clickable(enabled = !state.preparing) { onTarget(target) }.padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(if (target == ShareTarget.Copy) Icons.Outlined.ContentCopy else Icons.Outlined.MoreHoriz, null)
                    Text(stringResource(if (target == ShareTarget.Copy) (if (content.link != null) R.string.share_copy_link else content.copyLabel) else target.label), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun ShareTargetItem(target: ShareTarget, enabled: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val installedIcon = remember(target, context) {
        val packageName = if (target == ShareTarget.Messages) Telephony.Sms.getDefaultSmsPackage(context) else target.packageName
        packageName?.let { runCatching { context.packageManager.getApplicationIcon(it).toBitmap(144, 144).asImageBitmap() }.getOrNull() }
    }
    Column(Modifier.width(76.dp).clip(MaterialTheme.shapes.medium).clickable(enabled = enabled, onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
            when {
                installedIcon != null -> Image(installedIcon, null, Modifier.size(44.dp))
                target == ShareTarget.Feed -> Icon(painterResource(R.drawable.buddy_icon_flat), null, Modifier.size(32.dp))
                else -> Icon(if (target == ShareTarget.Instagram) Icons.Outlined.CameraAlt else Icons.AutoMirrored.Outlined.Chat, null, Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(target.label), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ko", fontScale = 1.4f)
@Composable
private fun ShareSheetPreview() {
    Theme {
        ShareSheetContent(ShareContent(stringResource(R.string.timetable_share), "", "#FFFFFF", "#FFFFFF"), ShareState(), true, {})
    }
}
