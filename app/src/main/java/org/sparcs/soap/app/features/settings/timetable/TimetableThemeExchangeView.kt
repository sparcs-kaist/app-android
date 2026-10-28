package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.navigationBar.components.CloseButton
import org.sparcs.soap.app.features.settings.timetable.components.ThemePreview
import org.sparcs.soap.app.features.settings.timetable.components.displayName
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareCard
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareImagePreview
import org.sparcs.soap.app.shared.sharing.ShareSheet
import org.sparcs.soap.app.shared.sharing.StoryBackground
import org.sparcs.soap.app.shared.sharing.StoryBackgroundOptions
import org.sparcs.soap.app.shared.sharing.navigateToShareFeed
import org.sparcs.soap.app.theme.ui.Theme

// Import and sharing follow the iOS app: each is its own screen that does the network work itself,
// so it opens straight away and reports loading, success and failure in place.

/**
 * Asks for a share code before opening the import screen, starting from [initialCode] when the
 * last one wasn't found. Continue stays disabled until it's valid.
 */
@Composable
fun TimetableThemeCodeDialog(onDismiss: () -> Unit, onContinue: (String) -> Unit, initialCode: String = "") {
    var code by rememberSaveable { mutableStateOf(initialCode) }
    val normalized = TimetableThemeShareCode.normalized(code)
    val focus = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_import_title)) },
        text = {
            // Inside the dialog's own content, so the field is attached when focus is requested.
            LaunchedEffect(Unit) { focus.requestFocus() }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.theme_import_instructions))
                OutlinedTextField(
                    value = code,
                    // Share codes are six upper-case ASCII letters and digits.
                    onValueChange = { input ->
                        code = input.filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }.uppercase().take(6)
                    },
                    label = { Text(stringResource(R.string.theme_share_code)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { normalized?.let(onContinue) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { normalized?.let(onContinue) }, enabled = normalized != null) {
                Text(stringResource(R.string.theme_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/**
 * The import screen for [initialCode]. A code that isn't found can be re-entered here, which looks
 * up the new code in place instead of going back to the dialog.
 */
@Composable
fun TimetableThemeImportRoute(
    initialCode: String,
    onClose: () -> Unit,
    onImport: (TimetableTheme) -> Unit,
    viewModel: TimetableThemeExchangeViewModel = hiltViewModel(),
) {
    var code by rememberSaveable { mutableStateOf(initialCode) }
    var reenteringCode by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(code) { viewModel.fetch(code) }
    TimetableThemeImportView(
        code = code,
        state = viewModel.state,
        onClose = onClose,
        onImport = onImport,
        onRetry = { viewModel.fetch(code) },
        onReenterCode = { reenteringCode = true }
    )
    if (reenteringCode) {
        TimetableThemeCodeDialog(
            onDismiss = { reenteringCode = false },
            onContinue = { newCode ->
                reenteringCode = false
                // The same code again still deserves a fresh lookup.
                if (newCode == code) viewModel.fetch(code) else code = newCode
            },
            initialCode = code
        )
    }
}

/** Looks up the theme behind [code], previews it, and adds it to My Themes. */
@Composable
internal fun TimetableThemeImportView(
    code: String,
    state: ThemeExchangeState,
    onClose: () -> Unit,
    onImport: (TimetableTheme) -> Unit,
    onRetry: () -> Unit,
    onReenterCode: () -> Unit = {},
) {
    val theme = state.theme
    ExchangeScaffold(
        title = stringResource(R.string.theme_import_title),
        screen = "TimetableThemeImport",
        onClose = onClose,
        centered = theme == null,
        button = {
            when {
                state.loading -> ExchangeButton(R.string.theme_add, Icons.Outlined.Download, enabled = false) {}
                theme != null -> ExchangeButton(R.string.theme_add, Icons.Outlined.Download) { onImport(theme) }
                // A mistyped code fails the same way every time, so offer to fix it instead.
                state.isCodeNotFound -> ExchangeButton(R.string.theme_reenter_code, Icons.Outlined.Edit, onClick = onReenterCode)
                else -> ExchangeButton(R.string.theme_retry, Icons.Outlined.Refresh, onClick = onRetry)
            }
        }
    ) {
        when {
            state.loading -> ExchangeProgress(stringResource(R.string.theme_import_looking, code))
            theme != null -> {
                ThemePreview(theme)
                ExchangeMessage {
                    Text(
                        theme.displayName(),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    ExchangeNote(stringResource(R.string.theme_import_note))
                }
            }
            else -> ExchangeFailure(stringResource(state.error ?: R.string.theme_import_error))
        }
    }
}

@Composable
fun TimetableThemeSharingRoute(
    theme: TimetableTheme,
    onClose: () -> Unit,
    navController: NavController,
    viewModel: TimetableThemeExchangeViewModel = hiltViewModel(),
) {
    LaunchedEffect(theme.id) { viewModel.share(theme) }
    TimetableThemeSharingView(
        theme = theme,
        state = viewModel.state,
        onClose = onClose,
        onRetry = { viewModel.share(theme) },
        navController = navController
    )
}

/** Uploads [theme], shows the code someone else types to import it, and shares it as an image. */
@Composable
internal fun TimetableThemeSharingView(
    theme: TimetableTheme,
    state: ThemeExchangeState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    navController: NavController? = null,
) {
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    val name = theme.displayName()
    val code = state.code
    ExchangeScaffold(
        title = stringResource(R.string.theme_sharing_title, name),
        screen = "TimetableThemeSharing",
        onClose = onClose,
        button = {
            when {
                state.loading -> ExchangeButton(R.string.share, Icons.Outlined.Share, enabled = false) {}
                code != null -> ExchangeButton(R.string.share, Icons.Outlined.Share) { showShareSheet = true }
                else -> ExchangeButton(R.string.theme_retry, Icons.Outlined.Refresh, onClick = onRetry)
            }
        }
    ) {
        ThemePreview(theme)
        when {
            state.loading -> ExchangeProgress(stringResource(R.string.theme_sharing_progress, name))
            code != null -> ExchangeMessage {
                ShareCodeButton(code)
                ExchangeNote(stringResource(R.string.theme_share_instructions))
            }
            else -> ExchangeFailure(stringResource(state.error ?: R.string.theme_share_error))
        }
    }

    if (showShareSheet && code != null) {
        ThemeShareSheet(theme, code, navController) { showShareSheet = false }
    }
}

/** The code in large monospaced type; tapping copies it, and the icon confirms for two seconds. */
@Composable
private fun ShareCodeButton(code: String) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
    val copyLabel = stringResource(R.string.theme_copy_code)
    Surface(
        onClick = {
            clipboard.setText(AnnotatedString(code))
            copied = true
        },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.semantics { contentDescription = "$code, $copyLabel" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(code, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Monospace)
            Icon(if (copied) Icons.Filled.Check else Icons.Outlined.ContentCopy, contentDescription = null)
        }
    }
}

/** The existing share sheet: Instagram Story, Feed, other apps and copy, with the theme card as the image. */
@Composable
private fun ThemeShareSheet(
    theme: TimetableTheme,
    code: String,
    navController: NavController?,
    onDismiss: () -> Unit,
) {
    val shareLayer = rememberGraphicsLayer()
    var background by rememberSaveable(theme.id) { mutableStateOf(StoryBackground.initial(theme)) }
    val (top, bottom) = background.colors(theme)
    val sample = rememberThemeSample()
    ShareSheet(
        content = ShareContent(
            title = stringResource(R.string.theme_share),
            text = stringResource(R.string.theme_share_feed_content, theme.displayName(), code),
            topColor = top,
            bottomColor = bottom,
            copyText = code,
            copyLabel = R.string.theme_copy_code,
        ),
        capture = { shareLayer.toImageBitmap().asAndroidBitmap() },
        onDismiss = onDismiss,
        options = { enabled -> StoryBackgroundOptions(theme, background, enabled) { background = it } },
        preview = {
            ShareImagePreview(
                widthDp = 440, heightDp = 600,
                background = Brush.verticalGradient(
                    listOf(TimetableTheme.color(top.removePrefix("#")), TimetableTheme.color(bottom.removePrefix("#")))
                )
            ) {
                Box(Modifier.drawWithContent {
                    shareLayer.record { this@drawWithContent.drawContent() }
                    drawContent()
                }) {
                    ThemeShareRenderingView(theme, code, sample.map { it.lecture }.distinctBy { it.courseID })
                }
            }
        },
        onFeed = navController?.let { controller ->
            { uri, text -> controller.navigateToShareFeed(uri, text) }
        },
    )
}

@Composable
private fun ThemeShareRenderingView(theme: TimetableTheme, code: String, lectures: List<Lecture>) {
    TimetableShareCard(
        theme, Timetable(id = "theme-share", lectures = lectures),
        stringResource(R.string.share_timetable_theme), theme.displayName(),
        stringResource(R.string.code), code
    )
}

/**
 * A close button and title on top, the scrolling [content] in the middle, and one full-width
 * [button] pinned to the bottom, as on the iOS sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeScaffold(
    title: String,
    screen: String,
    onClose: () -> Unit,
    button: @Composable () -> Unit,
    centered: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier.analyticsScreen(screen),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                navigationIcon = { CloseButton(onClick = onClose) },
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) { button() }
        }
    ) { padding ->
        // Loading and failure have no preview above them, so they sit in the middle like iOS.
        Box(
            contentAlignment = if (centered) Alignment.Center else Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                content = content
            )
        }
    }
}

/** Full width on a phone, capped so it doesn't stretch across a tablet. */
@Composable
private fun ExchangeButton(label: Int, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(label))
    }
}

@Composable
private fun ExchangeMessage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun ExchangeNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ExchangeProgress(text: String) {
    ExchangeMessage {
        CircularProgressIndicator()
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExchangeFailure(message: String) {
    ExchangeMessage {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        ExchangeNote(message)
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportLoadingPreview() {
    Theme { TimetableThemeImportView("ABC123", ThemeExchangeState(loading = true), {}, {}, {}) }
}

@Preview(showBackground = true, heightDp = 891)
@Composable
private fun ImportFoundPreview() {
    Theme {
        TimetableThemeImportView(
            "ABC123",
            ThemeExchangeState(theme = TimetableTheme.Default.duplicate(stringResource(R.string.theme_ocean))),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportNotFoundPreview() {
    Theme { TimetableThemeImportView("ABC123", ThemeExchangeState(error = R.string.theme_code_not_found, isCodeNotFound = true), {}, {}, {}) }
}

@Preview(showBackground = true, heightDp = 891)
@Composable
private fun SharingPreview() {
    Theme {
        TimetableThemeSharingView(
            TimetableTheme.Default.duplicate("My Theme"),
            ThemeExchangeState(code = "ABC123"),
            {}, {}, rememberNavController()
        )
    }
}

@Preview(showBackground = true, heightDp = 891)
@Composable
private fun SharingLoadingPreview() {
    Theme {
        TimetableThemeSharingView(TimetableTheme.Default.duplicate("My Theme"), ThemeExchangeState(loading = true), {}, {})
    }
}
