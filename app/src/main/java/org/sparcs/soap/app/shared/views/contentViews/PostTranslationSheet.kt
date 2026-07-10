package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.translation.TranslationState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTranslationSheet(
    state: TranslationState,
    targetLanguage: String,
    languages: List<String>,
    suggested: List<String>,
    onTargetChange: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var picking by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (picking) {
            LanguageList(
                languages = languages,
                suggested = suggested,
                onSelect = {
                    picking = false
                    onTargetChange(it)
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Text(
                    text = stringResource(R.string.translate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 460.dp)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (state) {
                        TranslationState.Idle -> Unit

                        TranslationState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = stringResource(R.string.translating),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is TranslationState.Translated -> {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = stringResource(
                                        R.string.translated_from,
                                        displayLanguageName(state.sourceLanguage)
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.padding(top = 4.dp))
                                Text(
                                    text = state.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        TranslationState.Unsupported -> {
                            Text(
                                text = stringResource(R.string.translation_unsupported),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TranslationState.Failed -> {
                            Column {
                                Text(
                                    text = stringResource(R.string.translation_failed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onRetry) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.translate_to),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = { picking = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(displayLanguageName(targetLanguage))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageList(
    languages: List<String>,
    suggested: List<String>,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, languages) {
        if (query.isBlank()) {
            languages
        } else {
            languages.filter { code ->
                displayLanguageName(code).contains(query, ignoreCase = true) ||
                    code.contains(query, ignoreCase = true)
            }
        }
    }
    val showSuggested = query.isBlank() && suggested.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Text(
            text = stringResource(R.string.translate_to),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        SearchCustomBar(
            value = query,
            onValueChange = { query = it },
            onValueClear = { query = "" },
            placeHolder = stringResource(R.string.search)
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
            if (showSuggested) {
                item { SectionHeader(stringResource(R.string.suggested)) }
                items(suggested) { LanguageRow(it, onSelect) }
                item { SectionHeader(stringResource(R.string.all_languages)) }
            }
            items(filtered) { LanguageRow(it, onSelect) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun LanguageRow(code: String, onSelect: (String) -> Unit) {
    Text(
        text = displayLanguageName(code),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(code) }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

private fun displayLanguageName(code: String): String =
    Locale.forLanguageTag(code).getDisplayLanguage(Locale.getDefault()).ifBlank { code }
