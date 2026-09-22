package org.sparcs.soap.app.features.settings.timetable

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetablePhotoDecoder
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.usecases.ThemeModelStatus
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.timetable.components.AdvancedColorPicker
import org.sparcs.soap.app.features.settings.timetable.components.ThemeAdvancedColors
import org.sparcs.soap.app.features.settings.timetable.components.ThemeColorRow
import org.sparcs.soap.app.features.settings.timetable.components.ThemeNameField
import org.sparcs.soap.app.features.settings.timetable.components.ThemePaletteRow
import org.sparcs.soap.app.features.settings.timetable.components.ThemePhotoErrorDialog
import org.sparcs.soap.app.features.settings.timetable.components.ThemePhotoProgressDialog
import org.sparcs.soap.app.features.settings.timetable.components.ThemePhotoSection
import org.sparcs.soap.app.features.settings.timetable.components.ThemePreview
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSectionTitle
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSettingsList
import org.sparcs.soap.app.features.settings.timetable.components.rememberPaletteDragState
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableThemeEditor(
    seed: String,
    onBack: () -> Unit,
    onSave: (TimetableTheme) -> Unit,
) {
    var draftJson by rememberSaveable(seed) { mutableStateOf(seed) }
    val draft = remember(draftJson) { Json.decodeFromString<TimetableTheme>(draftJson) }
    var savedJson by rememberSaveable(seed) { mutableStateOf(seed) }
    var showDiscardDialog by rememberSaveable(seed) { mutableStateOf(false) }
    var paletteJson by rememberSaveable(seed) {
        mutableStateOf(Json.encodeToString(TimetableThemePalette.from(draft.hexColors)))
    }
    val palette = remember(paletteJson) {
        Json.decodeFromString<TimetableThemePalette>(paletteJson)
    }

    fun update(theme: TimetableTheme) {
        draftJson = Json.encodeToString(theme)
    }

    fun updatePalette(updated: TimetableThemePalette) {
        paletteJson = Json.encodeToString(updated)
        update(draft.copy(hexColors = updated.hexColors))
    }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val generator: TimetableThemeGeneratorViewModel = viewModel(key = "theme-generator-$seed")
    val generatorState by generator.state.collectAsState()
    var showGenerator by rememberSaveable(seed) { mutableStateOf(false) }
    ThemeGeneratorLifecycleEffect(generator)
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var photoError by remember { mutableStateOf(false) }
    val isGenerating = selectedPhoto != null
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedPhoto = uri
        }
    LaunchedEffect(selectedPhoto) {
        val uri = selectedPhoto ?: return@LaunchedEffect
        try {
            val generated = TimetablePhotoDecoder.generate(context.contentResolver, uri)
            currentCoroutineContext().ensureActive()
            paletteJson = Json.encodeToString(TimetableThemePalette.from(generated.colors))
            update(generated.applyTo(draft))
            listState.animateScrollToItem(0)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            photoError = true
        } finally {
            if (selectedPhoto == uri) selectedPhoto = null
        }
    }
    val dragState =
        rememberPaletteDragState(listState, palette.colors.map { it.id }) { source, target ->
            val sourceIndex = palette.colors.indexOfFirst { it.id == source }
            val targetIndex = palette.colors.indexOfFirst { it.id == target }
            if (sourceIndex >= 0 && targetIndex >= 0) {
                updatePalette(palette.move(source, targetIndex - sourceIndex))
            }
        }

    var expanded by rememberSaveable { mutableStateOf(false) }
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    val back = {
        if (draftJson != savedJson) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }
    BackHandler(enabled = !showGenerator, onBack = back)

    if (showGenerator) {
        TimetableThemeGeneratorView(
            state = generatorState,
            baseTheme = draft,
            onDescriptionChange = generator::describe,
            onGenerate = { generator.generate(draft, it) },
            onDownload = generator::download,
            onRefresh = generator::refreshAvailability,
            onCancel = generator::cancel,
            onBack = {
                generator.cancel()
                showGenerator = false
            },
            onApply = { theme ->
                paletteJson = Json.encodeToString(TimetableThemePalette.from(theme.hexColors))
                update(theme)
                expanded = false
                generator.reset()
                showGenerator = false
            },
        )
        return
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.theme_edit),
                onDismiss = back,
                isEditable = true,
                isDoneEnabled = draft.isValid && !isGenerating,
                onClickDone = {
                    onSave(draft)
                    savedJson = draftJson
                    onBack()
                }
            )
        }
    ) { padding ->
        ThemeSettingsList(
            padding = padding,
            modifier = Modifier.imePadding(),
            maxWidth = 680.dp,
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ThemePreview(draft) }
            item {
                ThemeNameField(draft.name) { update(draft.copy(name = it)) }
            }
            item {
                ThemeColorRow(
                    stringResource(R.string.theme_text),
                    draft.textColorHex
                ) { target = "text" }
            }
            item {
                ThemeSectionTitle(stringResource(R.string.theme_palette))
                Text(
                    stringResource(
                        R.string.theme_palette_description,
                        TimetableTheme.maximumColors
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(palette.colors, key = { _, color -> color.id }) { index, color ->
                ThemePaletteRow(
                    color = color,
                    index = index,
                    count = palette.colors.size,
                    onEdit = { target = color.id },
                    onMove = { offset -> updatePalette(palette.move(color.id, offset)) },
                    onDelete = {
                        updatePalette(palette.remove(color.id))
                    },
                    dragState = dragState,
                    modifier = (if (dragState.draggedId == color.id) Modifier else Modifier.animateItem())
                        .zIndex(if (dragState.draggedId == color.id) 1f else 0f)
                        .graphicsLayer { translationY = dragState.translation(color.id) }
                )
            }
            item {
                OutlinedButton(
                    onClick = { updatePalette(palette.add()) },
                    enabled = palette.canAdd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.theme_add_color))
                }
            }
            item {
                ThemeAdvancedColors(
                    draft = draft,
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                    update = ::update,
                    onPick = { target = it }
                )
            }
            item { ThemePhotoSection(onPick = { photoPicker.launch("image/*") }) }
            if (generatorState.modelStatus != ThemeModelStatus.CHECKING &&
                generatorState.modelStatus != ThemeModelStatus.UNAVAILABLE
            ) {
                item {
                    ThemeGenerationSection(onOpen = {
                        showGenerator = true
                        generator.refreshAvailability()
                    })
                }
            }
        }
    }
    if (isGenerating) ThemePhotoProgressDialog(onCancel = { selectedPhoto = null })
    if (photoError) ThemePhotoErrorDialog(onDismiss = { photoError = false })
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.theme_discard_title)) },
            text = { Text(stringResource(R.string.theme_unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = onBack) { Text(stringResource(R.string.theme_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    target?.let { key ->
        val hex = when (key) {
            "text" -> draft.textColorHex
            "separator" -> draft.separatorColorHex
            "background" -> draft.backgroundColorHex
            "labels" -> draft.gridLabelColorHex
            else -> palette.colors.firstOrNull { it.id == key }?.hex
        }
        if (hex != null) AdvancedColorPicker(
            hex,
            onDismiss = { target = null },
            onApply = { value ->
                when (key) {
                    "text" -> update(draft.copy(textColorHex = value))
                    "separator" -> update(draft.copy(separatorColorHex = value))
                    "background" -> update(draft.copy(backgroundColorHex = value))
                    "labels" -> update(draft.copy(gridLabelColorHex = value))
                    else -> updatePalette(palette.update(key, value))
                }
                target = null
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableThemeEditorPreview() {
    Theme {
        TimetableThemeEditor(
            seed = Json.encodeToString(TimetableTheme.Default),
            onBack = {},
            onSave = {}
        )
    }
}
