package org.sparcs.soap.app.features.settings.timetable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeBrief
import org.sparcs.soap.app.domain.usecases.ThemeGenerationError
import org.sparcs.soap.app.domain.usecases.ThemeModelStatus
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ThemeGenerationSection(onOpen: () -> Unit) {
    ThemeSettingsAction(
        text = stringResource(R.string.theme_ai_title),
        icon = Icons.Outlined.AutoAwesome,
        onClick = onOpen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimetableThemeGeneratorView(
    state: ThemeGeneratorState,
    baseTheme: TimetableTheme,
    onDescriptionChange: (String) -> Unit,
    onGenerate: (String) -> Unit,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onApply: (TimetableTheme) -> Unit,
) {
    BackHandler(onBack = onBack)
    val prompt = stringResource(R.string.theme_ai_prompt, state.description.trim())
    val descriptionLabel = stringResource(R.string.theme_ai_describe)
    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.theme_ai_generate),
                onDismiss = onBack,
                isEditable = true,
                isDoneEnabled = state.ready && !state.generating && !state.downloading && state.preview != null,
                onClickDone = { if (state.ready) state.preview?.let(onApply) },
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "preview") {
                    Column(Modifier.padding(bottom = 24.dp)) {
                        ThemeSettingsSectionTitle(stringResource(R.string.theme_preview))
                        ThemePreview(state.preview ?: baseTheme)
                        state.preview?.let { theme ->
                            Text(
                                theme.name,
                                Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                item(key = "description") {
                    ThemeSettingsSectionTitle(stringResource(R.string.theme_ai_describe))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = descriptionLabel },
                        enabled = !state.generating && !state.downloading,
                        placeholder = { Text(stringResource(R.string.theme_ai_example)) },
                        supportingText = {
                            Text(
                                stringResource(
                                    R.string.theme_ai_character_count,
                                    state.description.length,
                                    TimetableThemeBrief.maximumDescriptionLength
                                )
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                    )
                    Text(
                        stringResource(R.string.theme_ai_description),
                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.modelStatus != ThemeModelStatus.AVAILABLE && !state.downloading) item(key = "model") {
                    Column(Modifier.padding(8.dp)) {
                        ThemeModelStatusView(
                            state,
                            onDownload,
                            onRefresh
                        )
                    }
                }
                if (state.generating || state.downloading) {
                    item(key = "progress") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(
                                stringResource(if (state.generating) R.string.theme_ai_generating else R.string.theme_ai_downloading),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { liveRegion = LiveRegionMode.Polite },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                        }
                    }
                } else {
                    item(key = "generate") {
                        Button(
                            onClick = { onGenerate(prompt) },
                            enabled = state.canGenerate,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (state.ready) Icons.Outlined.Refresh else Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                )
                                Text(stringResource(if (state.ready) R.string.theme_ai_again else R.string.theme_ai_generate))
                            }
                        }
                    }
                }
                state.error?.let { error ->
                    item {
                        Text(
                            text = stringResource(error.messageResource()),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item(key = "footer") {
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    Text(
                        stringResource(R.string.theme_ai_privacy),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModelStatusView(
    state: ThemeGeneratorState,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
) {
    if (state.downloading) return
    when (state.modelStatus) {
        ThemeModelStatus.AVAILABLE -> Unit
        ThemeModelStatus.CHECKING -> Text(stringResource(R.string.theme_ai_checking))
        ThemeModelStatus.DOWNLOADABLE -> Column {
            Text(stringResource(R.string.theme_ai_download_description))
            TextButton(onClick = onDownload) { Text(stringResource(R.string.theme_ai_download)) }
        }

        ThemeModelStatus.DOWNLOADING -> Column {
            Text(stringResource(R.string.theme_ai_downloading))
            TextButton(onClick = onDownload) { Text(stringResource(R.string.theme_ai_check_download)) }
        }

        ThemeModelStatus.UNAVAILABLE -> Column {
            Text(stringResource(R.string.theme_ai_unavailable))
            TextButton(onClick = onRefresh) { Text(stringResource(R.string.theme_ai_refresh)) }
        }
    }
}

private fun ThemeGenerationError.messageResource(): Int = when (this) {
    ThemeGenerationError.UNAVAILABLE -> R.string.theme_ai_unavailable
    ThemeGenerationError.UNSAFE_REQUEST -> R.string.theme_ai_unsafe
    ThemeGenerationError.INCOMPLETE -> R.string.theme_ai_incomplete
    ThemeGenerationError.FAILED -> R.string.theme_ai_failed
    ThemeGenerationError.BUSY -> R.string.theme_ai_failed
    ThemeGenerationError.QUOTA_EXCEEDED -> R.string.theme_ai_quota_exceeded
    ThemeGenerationError.DOWNLOAD_FAILED -> R.string.theme_ai_download_failed
}

@Preview(showBackground = true)
@Composable
private fun ThemeGeneratorPreview() {
    Theme {
        TimetableThemeGeneratorView(
            state = ThemeGeneratorState(modelStatus = ThemeModelStatus.AVAILABLE),
            baseTheme = TimetableTheme.Default,
            onDescriptionChange = {}, onGenerate = {}, onDownload = {}, onRefresh = {},
            onCancel = {}, onBack = {}, onApply = {},
        )
    }
}
