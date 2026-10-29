package org.sparcs.soap.app.features.post.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.post.PostTranslator
import org.sparcs.soap.app.shared.views.contentViews.MaterialSearchField
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    targets: List<String>,
    suggested: List<String>,
    current: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        LanguagePicker(
            targets = targets,
            suggested = suggested,
            current = current,
            onSelect = onSelect,
        )
    }
}

@Composable
fun LanguagePicker(
    targets: List<String>,
    suggested: List<String>,
    current: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
) {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    val sections = remember(targets, suggested, current, query) {
        PostTranslator.pickerSections(targets, suggested, current, query, ::languageName, ::languageAliases)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.translate_to),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        if (targets.isEmpty()) {
            PickerMessage(stringResource(R.string.translate_no_languages))
            return@Column
        }
        MaterialSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(R.string.translate_search_languages),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (sections.isEmpty) {
            PickerMessage(stringResource(R.string.translate_no_matches, query.trim()))
            return@Column
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            languageGroup(R.string.suggested.takeIf { query.isBlank() }, sections.suggested, current, onSelect)
            languageGroup(R.string.all_languages.takeIf { query.isBlank() }, sections.others, current, onSelect)
        }
    }
}

private fun LazyListScope.languageGroup(
    header: Int?,
    codes: List<String>,
    current: String?,
    onSelect: (String) -> Unit,
) {
    if (codes.isEmpty()) return
    if (header != null) {
        item(key = "header-$header") { GroupHeader(stringResource(header)) }
    }
    items(codes, key = { "$header-$it" }) { code ->
        LanguageRow(code = code, isSelected = code == current, onClick = { onSelect(code) })
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun LanguageRow(code: String, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = languageName(code),
                fontWeight = if (isSelected) FontWeight.SemiBold else null,
            )
        },
        trailingContent = if (isSelected) {
            { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
    )
}

@Composable
private fun PickerMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
    )
}

private fun languageAliases(code: String): List<String> {
    val locale = Locale.forLanguageTag(code)
    return listOf(locale.getDisplayLanguage(Locale.ENGLISH), locale.getDisplayLanguage(locale))
}

private val previewTargets = listOf("en", "ja", "zh", "ar", "he", "fr", "de", "es", "vi", "th", "ru", "hi")

@Composable
private fun PickerPreviewSurface(content: @Composable () -> Unit) {
    Theme {
        Surface(color = MaterialTheme.colorScheme.surface) { content() }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun LanguagePickerPreview() {
    PickerPreviewSurface {
        LanguagePicker(targets = previewTargets, suggested = listOf("ko", "en", "ja"), current = "ja", onSelect = {})
    }
}

@Preview(showBackground = true, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguagePickerDarkPreview() {
    PickerPreviewSurface {
        LanguagePicker(targets = previewTargets, suggested = listOf("en"), current = null, onSelect = {})
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun LanguagePickerRtlPreview() {
    PickerPreviewSurface {
        LanguagePicker(targets = previewTargets.take(5), suggested = listOf("ar"), current = "ar", onSelect = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguagePickerNoMatchesPreview() {
    PickerPreviewSurface {
        LanguagePicker(targets = previewTargets, suggested = listOf("en"), current = null, onSelect = {}, initialQuery = "xyz")
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguagePickerEmptyPreview() {
    PickerPreviewSurface {
        LanguagePicker(targets = emptyList(), suggested = emptyList(), current = null, onSelect = {})
    }
}
