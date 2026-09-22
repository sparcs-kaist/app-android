package org.sparcs.soap.app.features.settings.timetable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Feed
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.InstagramShareHelper
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.timetable.components.ThemePreview
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSettingsList
import org.sparcs.soap.app.features.settings.timetable.components.displayName
import org.sparcs.soap.app.theme.ui.Theme
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

@Composable
fun TimetableThemeExchangeRoute(
    sharing: TimetableTheme?,
    onBack: () -> Unit,
    onImport: (TimetableTheme) -> Unit,
    navController: NavController,
    viewModel: TimetableThemeExchangeViewModel = hiltViewModel(),
) {
    LaunchedEffect(sharing) {
        viewModel.reset()
        sharing?.let(viewModel::share)
    }
    DisposableEffect(viewModel) { onDispose { viewModel.reset() } }
    TimetableThemeExchangeView(
        sharing = sharing,
        state = viewModel.state,
        onBack = onBack,
        onImport = onImport,
        onFind = viewModel::fetch,
        onRetryShare = { sharing?.let(viewModel::share) },
        onCodeChanged = viewModel::reset,
        navController = navController
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TimetableThemeExchangeView(
    sharing: TimetableTheme?,
    state: ThemeExchangeState,
    onBack: () -> Unit,
    onImport: (TimetableTheme) -> Unit,
    onFind: (String) -> Unit,
    onRetryShare: () -> Unit,
    onCodeChanged: () -> Unit,
    navController: NavController? = null,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val graphicsLayer = rememberGraphicsLayer()

    BackHandler(onBack = onBack)
    Scaffold(topBar = {
        SettingsViewNavigationBar(
            title = stringResource(if (sharing != null) R.string.theme_share else R.string.theme_import),
            onDismiss = onBack
        )
    }) { padding ->
        ThemeSettingsList(
            padding = padding,
            maxWidth = 680.dp,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (sharing == null) item {
                ThemeImportForm(
                    input = input,
                    loading = state.loading,
                    onInputChange = {
                        input = it
                        onCodeChanged()
                    },
                    onFind = onFind
                )
            }
            val theme = sharing ?: state.theme
            if (theme != null) item {
                ThemeShareCardView(
                    theme = theme,
                    code = state.code,
                    graphicsLayer = graphicsLayer
                )
            }
            if (state.loading) item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.theme_loading))
                }
            }
            state.error?.let { error ->
                item {
                    Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(
                        onClick = { if (sharing != null) onRetryShare() else onFind(input) },
                        enabled = sharing != null || TimetableThemeShareCode.normalized(input) != null
                    ) { Text(stringResource(R.string.theme_retry)) }
                }
            }
            if (sharing != null) state.code?.let { code ->
                item {
                    ThemeShareActions(
                        theme = sharing,
                        code = code,
                        graphicsLayer = graphicsLayer,
                        navController = navController
                    )
                }
            }
            if (sharing == null && state.theme != null && !state.loading) item {
                Text(stringResource(R.string.theme_import_note))
                Button(
                    onClick = { onImport(state.theme) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.theme_save_import))
                }
            }
        }
    }
}

@Composable
private fun ThemeShareCardView(
    theme: TimetableTheme,
    code: String?,
    graphicsLayer: GraphicsLayer,
) {
    val cardBgColor = theme.backgroundColor ?: MaterialTheme.colorScheme.surface
    val isDark = isColorDark(cardBgColor)

    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val codeBgColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF0F172A).copy(alpha = 0.08f)
    val codeTextColor = if (isDark) Color.White else Color(0xFF0F172A)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawContent()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_buddy_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = "BUDDY TIMETABLE THEME",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = theme.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (code != null) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = codeTextColor,
                        modifier = Modifier
                            .background(codeBgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            ThemePreview(theme = theme)
        }
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance < 0.5f
}

@Composable
private fun ThemeImportForm(
    input: String,
    loading: Boolean,
    onInputChange: (String) -> Unit,
    onFind: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.theme_import_instructions))
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            label = { Text(stringResource(R.string.theme_share_code)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                autoCorrectEnabled = false
            )
        )
        Button(
            onClick = { onFind(input) },
            enabled = !loading && TimetableThemeShareCode.normalized(input) != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.theme_find)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeShareActions(
    theme: TimetableTheme,
    code: String,
    graphicsLayer: GraphicsLayer,
    navController: NavController? = null,
) {
    var copied by rememberSaveable(code) { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shareTitle = stringResource(R.string.theme_share)
    val codeLabel = stringResource(R.string.theme_share_code)
    val feedContent = stringResource(R.string.theme_share_feed_content, theme.displayName(), code)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.theme_share_instructions),
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedButton(
            onClick = {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(codeLabel, code))
                copied = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(if (copied) R.string.theme_code_copied else R.string.theme_copy_code))
        }

        Button(
            onClick = { showShareSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.theme_share))
        }
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.theme_share),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showShareSheet = false
                            coroutineScope.launch {
                                val uri = captureAndSaveImage(context, graphicsLayer, code)
                                if (uri != null) {
                                    val topColor = theme.colorFor(0)
                                    val bottomColor = theme.colorFor(if (theme.hexColors.size > 1) 1 else 0)
                                    InstagramShareHelper.shareToInstagramStory(context, uri, topColor, bottomColor)
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.theme_share_instagram_story),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showShareSheet = false
                            coroutineScope.launch {
                                val uri = captureAndSaveImage(context, graphicsLayer, code)
                                if (uri != null && navController != null) {
                                    val encodedText = Uri.encode(feedContent)
                                    val encodedUri = Uri.encode(uri.toString())
                                    navController.navigate("${Channel.FeedPostCompose.name}?initial_text=$encodedText&initial_image_uri=$encodedUri")
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Feed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.theme_share_feed),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showShareSheet = false
                            coroutineScope.launch {
                                val uri = captureAndSaveImage(context, graphicsLayer, code)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_TEXT, feedContent)
                                    if (uri != null) {
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                }
                                context.startActivity(Intent.createChooser(intent, shareTitle))
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.theme_share_other_apps),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private suspend fun captureAndSaveImage(
    context: Context,
    graphicsLayer: GraphicsLayer,
    code: String,
): Uri? = withContext(Dispatchers.IO) {
    try {
        val imageBitmap = graphicsLayer.toImageBitmap()
        val bitmap = imageBitmap.asAndroidBitmap()
        val imagesDir = File(context.cacheDir, "shared_themes").apply { mkdirs() }
        val imageFile = File(imagesDir, "theme_share_$code.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        Timber.e(e, "Failed to capture Compose graphics layer to bitmap")
        null
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSharingPreview() {
    Theme {
        TimetableThemeExchangeView(
            sharing = TimetableTheme.Default,
            state = ThemeExchangeState(code = "ABC123"),
            onBack = {},
            onImport = {},
            onFind = {},
            onRetryShare = {},
            onCodeChanged = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun ThemeImportPreview() {
    Theme {
        TimetableThemeExchangeView(
            sharing = null,
            state = ThemeExchangeState(theme = TimetableTheme.Default.duplicate(stringResource(R.string.theme_ocean))),
            onBack = {},
            onImport = {},
            onFind = {},
            onRetryShare = {},
            onCodeChanged = {},
            navController = rememberNavController()
        )
    }
}
