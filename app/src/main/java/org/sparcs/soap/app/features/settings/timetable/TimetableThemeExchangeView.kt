package org.sparcs.soap.app.features.settings.timetable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.timetable.components.ThemePreview
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSettingsList
import org.sparcs.soap.app.features.settings.timetable.components.displayName
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TimetableThemeExchangeRoute(
    sharing: TimetableTheme?,
    onBack: () -> Unit,
    onImport: (TimetableTheme) -> Unit,
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
        onCodeChanged = viewModel::reset
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
) {
    var input by rememberSaveable { mutableStateOf("") }
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
                Text(theme.displayName(), style = MaterialTheme.typography.titleMedium)
                ThemePreview(theme)
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
                    ThemeShareCode(code)
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

@Composable
private fun ThemeShareCode(code: String) {
    var copied by rememberSaveable(code) { mutableStateOf(false) }
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.theme_share)
    val codeLabel = stringResource(R.string.theme_share_code)
    Text(
        code,
        style = MaterialTheme.typography.headlineLarge,
        fontFamily = FontFamily.Monospace
    )
    Text(stringResource(R.string.theme_share_instructions))
    OutlinedButton(onClick = {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(codeLabel, code))
        copied = true
    }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(if (copied) R.string.theme_code_copied else R.string.theme_copy_code))
    }
    Button(onClick = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, code)
        }
        context.startActivity(Intent.createChooser(intent, shareTitle))
    }, modifier = Modifier.fillMaxWidth()) { Text(shareTitle) }
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
            onCodeChanged = {}
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
            onCodeChanged = {}
        )
    }
}
