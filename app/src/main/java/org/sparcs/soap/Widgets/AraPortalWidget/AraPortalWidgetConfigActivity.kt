package org.sparcs.soap.Widgets.AraPortalWidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Ara.AraPortalNoticeType
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.WidgetEntryPoint

class AraPortalWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            Theme {
                AraPortalConfigScreen(
                    onDismiss = { finish() },
                    onSave = { theme, transparency, kwEnabled, kw, showTrending, selectedIds ->
                        saveAndFinish(theme, transparency, kwEnabled, kw, showTrending, selectedIds)
                    }
                )
            }
        }
    }

    @Composable
    private fun AraPortalConfigScreen(
        onDismiss: () -> Unit,
        onSave: (String, Float, Boolean, String, Boolean, Set<Int>) -> Unit
    ) {
        var selectedTheme by remember { mutableStateOf("System") }
        var transparency by remember { mutableFloatStateOf(1f) }
        var keywordEnabled by remember { mutableStateOf(false) }
        var keywords by remember { mutableStateOf("") }
        var showTrending by remember { mutableStateOf(true) }
        var selectedBoardIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
        var boards by remember { mutableStateOf<List<AraBoard>>(emptyList()) }
        var isBoardsExpanded by remember { mutableStateOf(false) }
        var isLoadingBoards by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isLoadingBoards = true
            val manager = GlanceAppWidgetManager(this@AraPortalWidgetConfigActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@AraPortalWidgetConfigActivity, glanceId) { prefs ->
                selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
                keywordEnabled = prefs[booleanPreferencesKey(KEYWORD_ENABLED_KEY)] ?: false
                keywords = prefs[stringPreferencesKey(KEYWORD_KEY)].orEmpty()
                showTrending = prefs[booleanPreferencesKey(SHOW_TRENDING_KEY)] ?: true
                selectedBoardIds = prefs[stringPreferencesKey(SELECTED_BOARD_IDS_KEY)]
                    ?.split(",")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toSet()
                    ?: emptySet()
            }

            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                WidgetEntryPoint::class.java
            )
            boards = runCatching { entryPoint.araBoardUseCase().fetchBoards() }
                .getOrElse { emptyList() }

            if (selectedBoardIds.isEmpty()) {
                val portalNoticeBoard = boards.find { it.slug == "portal-notice" }
                selectedBoardIds = if (portalNoticeBoard != null) {
                    setOf(portalNoticeBoard.id)
                } else {
                    emptySet()
                }
            }
            isLoadingBoards = false
        }

        Scaffold(
            topBar = {
                SettingsViewNavigationBar(
                    title = stringResource(R.string.ara_portal_widget_title),
                    onDismiss = onDismiss
                )
            }
        ) { innerPadding ->
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ConfigLazyList(
                    selectedTheme = selectedTheme,
                    transparency = transparency,
                    keywordEnabled = keywordEnabled,
                    keywords = keywords,
                    showTrending = showTrending,
                    selectedBoardIds = selectedBoardIds,
                    boards = boards,
                    isBoardsExpanded = isBoardsExpanded,
                    isLoadingBoards = isLoadingBoards,
                    onThemeChange = { selectedTheme = it },
                    onTransparencyChange = { transparency = it },
                    onKeywordEnabledChange = { keywordEnabled = it },
                    onKeywordsChange = { keywords = it },
                    onTrendingToggle = { isTrending ->
                        showTrending = isTrending
                        if (!isTrending && boards.isEmpty()) {
                            isLoadingBoards = true
                            lifecycleScope.launch {
                                val entryPoint = EntryPointAccessors.fromApplication(
                                    applicationContext,
                                    WidgetEntryPoint::class.java
                                )
                                boards = runCatching { entryPoint.araBoardUseCase().fetchBoards() }
                                    .getOrElse { emptyList() }
                                isLoadingBoards = false
                            }
                        }
                    },
                    onBoardsExpandToggle = { isBoardsExpanded = !isBoardsExpanded },
                    onBoardToggle = { boardId ->
                        selectedBoardIds = if (boardId in selectedBoardIds) {
                            selectedBoardIds - boardId
                        } else {
                            selectedBoardIds + boardId
                        }
                    },
                    onSelectAll = {
                        val allIds = AraPortalNoticeType.entries.filter { it != AraPortalNoticeType.Unknown }.map { it.id }.toSet() +
                                boards.map { it.id }.toSet()
                        selectedBoardIds = allIds
                    },
                    onDeselectAll = {
                        selectedBoardIds = emptySet()
                    },
                    onSave = {
                        onSave(selectedTheme, transparency, keywordEnabled, keywords, showTrending, selectedBoardIds)
                    }
                )
            }
        }
    }

    @Composable
    private fun ConfigLazyList(
        selectedTheme: String,
        transparency: Float,
        keywordEnabled: Boolean,
        keywords: String,
        showTrending: Boolean,
        selectedBoardIds: Set<Int>,
        boards: List<AraBoard>,
        isBoardsExpanded: Boolean,
        isLoadingBoards: Boolean,
        onThemeChange: (String) -> Unit,
        onTransparencyChange: (Float) -> Unit,
        onKeywordEnabledChange: (Boolean) -> Unit,
        onKeywordsChange: (String) -> Unit,
        onTrendingToggle: (Boolean) -> Unit,
        onBoardsExpandToggle: () -> Unit,
        onBoardToggle: (Int) -> Unit,
        onSelectAll: () -> Unit,
        onDeselectAll: () -> Unit,
        onSave: () -> Unit
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                AraPortalPreviewSection(selectedTheme, transparency)
                SectionTitle(stringResource(R.string.ara_portal_widget_content))
                KeywordRow(
                    enabled = keywordEnabled,
                    keywords = keywords,
                    onEnabledChange = onKeywordEnabledChange,
                    onKeywordsChange = onKeywordsChange,
                    disabled = showTrending
                )
                ToggleRow(
                    title = stringResource(R.string.ara_portal_widget_trending),
                    checked = showTrending,
                    onCheckedChange = onTrendingToggle
                )
                BoardSelection(
                    boards = boards,
                    selectedBoardIds = selectedBoardIds,
                    isExpanded = isBoardsExpanded,
                    isLoading = isLoadingBoards,
                    onExpandToggle = onBoardsExpandToggle,
                    onToggle = onBoardToggle,
                    onSelectAll = onSelectAll,
                    onDeselectAll = onDeselectAll,
                    disabled = showTrending
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                SectionTitle(stringResource(R.string.widget_miscellaneous))
                WidgetThemeRow(selectedTheme, onThemeChange)
                WidgetTransparencyRow(transparency, onTransparencyChange)
                Spacer(modifier = Modifier.height(16.dp))
                
                val isKeywordError = keywordEnabled && keywords.split(",").filter { it.isNotBlank() }.size > 3
                Button(
                    onClick = onSave,
                    enabled = !isKeywordError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(text = stringResource(R.string.save_configuration))
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        )
    }

    @Composable
    private fun KeywordRow(
        enabled: Boolean,
        keywords: String,
        onEnabledChange: (Boolean) -> Unit,
        onKeywordsChange: (String) -> Unit,
        disabled: Boolean = false
    ) {
        val keywordList = keywords.split(",").filter { it.isNotBlank() }
        val isError = keywordList.size > 3

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .alpha(if (disabled) 0.5f else 1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ara_portal_widget_keyword),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enabled, 
                    onCheckedChange = onEnabledChange,
                    enabled = !disabled
                )
            }
            if (enabled) {
                OutlinedTextField(
                    value = keywords,
                    onValueChange = onKeywordsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true,
                    enabled = !disabled,
                    label = { Text(stringResource(R.string.ara_portal_widget_keyword_hint)) },
                    placeholder = { Text("keyword1, keyword2, ...") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = stringResource(R.string.ara_portal_widget_max_keywords_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(text = stringResource(R.string.ara_portal_widget_keyword_description))
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = title, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }

    @Composable
    private fun BoardSelection(
        boards: List<AraBoard>,
        selectedBoardIds: Set<Int>,
        isExpanded: Boolean,
        isLoading: Boolean,
        onExpandToggle: () -> Unit,
        onToggle: (Int) -> Unit,
        onSelectAll: () -> Unit,
        onDeselectAll: () -> Unit,
        disabled: Boolean = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !disabled) { onExpandToggle() }
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .alpha(if (disabled) 0.5f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ara_portal_widget_boards),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null
            )
        }

        if (isExpanded && !disabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(R.string.config_select_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onSelectAll() }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.config_deselect_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.clickable { onDeselectAll() }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                val context = LocalContext.current
                
                val portalNoticeBoard = boards.find { it.slug == "portal-notice" }
                if (portalNoticeBoard != null) {
                    Text(
                        text = stringResource(R.string.portal_notice),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    BoardItemRow(
                        title = portalNoticeBoard.name.localized(),
                        isSelected = portalNoticeBoard.id in selectedBoardIds,
                        onToggle = { onToggle(portalNoticeBoard.id) }
                    )
                }
                
                Text(
                    text = stringResource(R.string.config_detailed_notice_category),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                AraPortalNoticeType.entries.filter { it != AraPortalNoticeType.Unknown }.forEach { board ->
                    BoardItemRow(
                        title = board.localizedString(context),
                        isSelected = board.id in selectedBoardIds,
                        onToggle = { onToggle(board.id) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                Text(
                    text = stringResource(R.string.config_community_board),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                if (boards.isEmpty()) {
                    Text(
                        text = stringResource(R.string.loading_data),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    val groupedBoards = boards.filter { apiBoard ->
                        AraPortalNoticeType.entries.none { it.id == apiBoard.id } && apiBoard.slug != "portal-notice"
                    }.groupBy { it.group.name.localized() }

                    groupedBoards.forEach { (groupName, boardsInGroup) ->
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        boardsInGroup.forEach { apiBoard ->
                            BoardItemRow(
                                title = apiBoard.name.localized(),
                                isSelected = apiBoard.id in selectedBoardIds,
                                onToggle = { onToggle(apiBoard.id) },
                                indent = 32.dp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BoardItemRow(
        title: String,
        isSelected: Boolean,
        onToggle: () -> Unit,
        indent: androidx.compose.ui.unit.Dp = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(start = indent, end = 16.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = title, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun AraPortalPreviewSection(selectedTheme: String, transparency: Float) {
        val isDark = when (selectedTheme) {
            "Dark" -> true
            "Light" -> false
            else -> isSystemInDarkTheme()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            SectionTitle(stringResource(R.string.preview))
            Theme(darkTheme = isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = transparency))
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.ara_portal_widget_header),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                        PreviewNotice(
                            stringResource(R.string.ara_portal_widget_preview_title_1),
                            stringResource(R.string.ara_portal_widget_trending_label),
                            stringResource(R.string.ara_portal_widget_preview_author_1)
                        )
                        PreviewNotice(
                            stringResource(R.string.ara_portal_widget_preview_title_2),
                            stringResource(R.string.ara_portal_widget_preview_board_2),
                            stringResource(R.string.ara_portal_widget_preview_author_2)
                        )
                        PreviewNotice(
                            stringResource(R.string.ara_portal_widget_preview_title_3),
                            stringResource(R.string.notice_board),
                            stringResource(R.string.ara_portal_widget_preview_author_3)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PreviewNotice(title: String, board: String, author: String) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(18.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Text(
                        text = board,
                        maxLines = 1,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = author,
                        maxLines = 1,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetThemeRow(selectedTheme: String, onThemeSelected: (String) -> Unit) {
        var showDialog by remember { mutableStateOf(false) }
        val currentModeText = when (selectedTheme) {
            "Light" -> stringResource(R.string.widget_white_mode)
            "Dark" -> stringResource(R.string.widget_dark_mode)
            else -> stringResource(R.string.widget_system_default)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.DarkMode, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = stringResource(R.string.theme_mode))
                Text(text = currentModeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.grayBB)
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.theme_mode)) },
                text = {
                    Column {
                        ThemeOptionRow(stringResource(R.string.widget_system_default), selectedTheme == "System") {
                            onThemeSelected("System"); showDialog = false
                        }
                        ThemeOptionRow(stringResource(R.string.widget_white_mode), selectedTheme == "Light") {
                            onThemeSelected("Light"); showDialog = false
                        }
                        ThemeOptionRow(stringResource(R.string.widget_dark_mode), selectedTheme == "Dark") {
                            onThemeSelected("Dark"); showDialog = false
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }

    @Composable
    private fun ThemeOptionRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 4.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Text(text)
        }
    }

    @Composable
    private fun WidgetTransparencyRow(transparency: Float, onTransparencyChange: (Float) -> Unit) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(R.string.transparency), modifier = Modifier.weight(1f))
                Text(text = "${(transparency * 100).toInt()}%", color = MaterialTheme.colorScheme.grayBB)
            }
            Slider(
                value = transparency,
                onValueChange = onTransparencyChange,
                valueRange = 0.0f..1.0f,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    private fun saveAndFinish(
        theme: String,
        transparency: Float,
        keywordEnabled: Boolean,
        keywords: String,
        showTrending: Boolean,
        selectedBoardIds: Set<Int>,
    ) {
        if (keywordEnabled && keywords.split(",").filter { it.isNotBlank() }.size > 3) {
            return
        }

        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@AraPortalWidgetConfigActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@AraPortalWidgetConfigActivity, glanceId) { prefs ->
                prefs[stringPreferencesKey("theme_mode")] = theme
                prefs[floatPreferencesKey("background_transparency")] = transparency
                prefs[booleanPreferencesKey(KEYWORD_ENABLED_KEY)] = keywordEnabled
                prefs[stringPreferencesKey(KEYWORD_KEY)] = keywords
                prefs[booleanPreferencesKey(SHOW_TRENDING_KEY)] = showTrending
                prefs[stringPreferencesKey(SELECTED_BOARD_IDS_KEY)] = selectedBoardIds.joinToString(",")
            }
            AraPortalWidget().update(this@AraPortalWidgetConfigActivity, glanceId)
            enqueueRefresh()

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private fun enqueueRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<AraPortalUpdateWorker>()
            .setConstraints(constraints)
            .addTag("ara_portal_config_refresh")
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "ara_portal_config_refresh",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
