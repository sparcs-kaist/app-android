package org.sparcs.soap.app.features.settings.timetable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSettingsList
import org.sparcs.soap.app.features.settings.timetable.components.displayName
import org.sparcs.soap.app.features.timetable.sharing.TimetableShareCard
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareImagePreview
import org.sparcs.soap.app.shared.sharing.ShareSheet
import org.sparcs.soap.app.shared.sharing.StoryBackground
import org.sparcs.soap.app.shared.sharing.StoryBackgroundOptions
import org.sparcs.soap.app.shared.sharing.navigateToShareFeed
import org.sparcs.soap.app.theme.ui.Theme

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
    val sample = rememberThemeSample()
    ShareImagePreview(widthDp = 440, heightDp = 600) {
        Box(Modifier.drawWithContent {
            graphicsLayer.record { this@drawWithContent.drawContent() }
            drawContent()
        }) {
            ThemeShareRenderingView(theme, code, sample.map { it.lecture }.distinctBy { it.courseID })
        }
    }
}

@Composable
private fun ThemeShareRenderingView(theme: TimetableTheme, code: String?, lectures: List<org.sparcs.soap.app.domain.models.otl.Lecture>) {
    TimetableShareCard(theme, Timetable(id = "theme-share", lectures = lectures),
        stringResource(R.string.share_timetable_theme), theme.displayName(),
        stringResource(R.string.code), code ?: "\u2014", isCode = true)
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
    navController: NavController? = null,
) {
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    val shareLayer = rememberGraphicsLayer()
    var background by rememberSaveable(theme.id) { mutableStateOf(StoryBackground.initial(theme)) }
    val (top, bottom) = background.colors(theme)
    val sample = rememberThemeSample()

    val shareTitle = stringResource(R.string.theme_share)
    val feedContent = stringResource(R.string.theme_share_feed_content, theme.displayName(), code)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.theme_share_instructions),
            style = MaterialTheme.typography.bodyMedium
        )

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
        ShareSheet(
            content = ShareContent(
                title = shareTitle,
                text = feedContent,
                topColor = top,
                bottomColor = bottom,
                copyText = code,
                copyLabel = R.string.theme_copy_code,
            ),
            capture = { shareLayer.toImageBitmap().asAndroidBitmap() },
            onDismiss = { showShareSheet = false },
            options = { enabled -> StoryBackgroundOptions(theme, background, enabled) { background = it } },
            preview = {
                ShareImagePreview(widthDp = 440, heightDp = 600,
                    background = Brush.verticalGradient(listOf(TimetableTheme.color(top.removePrefix("#")), TimetableTheme.color(bottom.removePrefix("#"))))) {
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
