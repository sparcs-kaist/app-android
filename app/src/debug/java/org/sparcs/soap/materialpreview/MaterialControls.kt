@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package org.sparcs.soap.materialpreview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterOption
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.settings.taxi.TaxiSettingsViewModel
import org.sparcs.soap.app.shared.extensions.toShortString
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.viewModelMocks.taxi.MockTaxiSettingsViewModel
import org.sparcs.soap.app.theme.ui.Theme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.sparcs.soap.app.shared.mocks.otl.mockList as otlMockList

// Material 3 "after" mockups for hand-built controls and list rows.
// Every preview mirrors the content/state of the original's @Preview (see the `// before:` comments).

// region Shared helpers

private val NoInsets = WindowInsets(0, 0, 0, 0)

@Composable
private fun MpSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun MpBackButton() {
    IconButton(onClick = {}) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

@Composable
private fun MpLogo(@DrawableRes id: Int) {
    // Brand logos keep their own colours, as in the original rows.
    Icon(
        painter = painterResource(id),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun MpReadOnlyDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = if (option == value) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                )
            }
        }
    }
}

@Composable
private fun MpSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
        windowInsets = NoInsets,
    ) {}
}

// endregion

// region Settings rows (features/settings/SettingsView.kt)

@Composable
private fun MpServiceNavButton(text: String, icon: @Composable () -> Unit, onClick: () -> Unit = {}) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = icon,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun MpAppSettings() {
    val context = LocalContext.current
    val languageDisplayName = context.resources.configuration.locales[0].displayLanguage
    ListItem(
        headlineContent = { Text(stringResource(R.string.change_language)) },
        supportingContent = { Text(languageDisplayName) },
        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
        modifier = Modifier.clickable {},
    )
}

@Composable
private fun MpThemeSwitcherButton(darkMode: Boolean?) {
    val currentModeText = when (darkMode) {
        true -> stringResource(R.string.widget_dark_mode)
        false -> stringResource(R.string.widget_white_mode)
        null -> stringResource(R.string.widget_system_default)
    }
    ListItem(
        headlineContent = { Text(stringResource(R.string.widget_dark_mode)) },
        supportingContent = { Text(currentModeText) },
        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
        modifier = Modifier.clickable {},
    )
}

@Composable
private fun MpSendCrashReportsButton(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.send_crash_reports)) },
        leadingContent = { Icon(Icons.Outlined.Lightbulb, contentDescription = null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    )
}

@Composable
private fun MpFeedbackButton() {
    ListItem(
        headlineContent = { Text(stringResource(R.string.chat_with_us)) },
        leadingContent = { Icon(Icons.Outlined.Feedback, contentDescription = null) },
        modifier = Modifier.clickable {},
    )
}

@Composable
private fun MpVersionRow() {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: Exception) {
        "0.0"
    }
    ListItem(
        headlineContent = { Text(stringResource(R.string.app_version)) },
        supportingContent = { Text(versionName) },
        leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
    )
}

@Composable
private fun MpSignOutButton() {
    ListItem(
        headlineContent = {
            Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error)
        },
        leadingContent = {
            Icon(
                Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        modifier = Modifier.clickable {},
    )
}

@Composable
private fun MpSettingsContent() {
    var isCrashlyticsEnabled by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        MpSectionHeader(stringResource(R.string.widget_miscellaneous))
        MpAppSettings()
        MpServiceNavButton(
            text = stringResource(R.string.notifications_title),
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
        )
        MpThemeSwitcherButton(darkMode = false) // MockSettingsViewModel.darkModeSetting = false
        MpSendCrashReportsButton(isCrashlyticsEnabled) { isCrashlyticsEnabled = it }
        HorizontalDivider()
        MpFeedbackButton()
        HorizontalDivider()

        MpSectionHeader(stringResource(R.string.services))
        MpServiceNavButton(stringResource(R.string.feed), { MpLogo(R.drawable.sparcs_logo) })
        MpServiceNavButton(stringResource(R.string.ara), { MpLogo(R.drawable.ara_logo) })
        MpServiceNavButton(stringResource(R.string.otl), { MpLogo(R.drawable.otl_logo) })
        MpServiceNavButton(stringResource(R.string.taxi), { MpLogo(R.drawable.taxi_logo) })
        HorizontalDivider()

        MpSectionHeader(stringResource(R.string.screen_style))
        MpServiceNavButton(
            text = stringResource(R.string.timetable_theme),
            icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
        )
        HorizontalDivider()

        MpSectionHeader(stringResource(R.string.information))
        MpServiceNavButton(
            text = stringResource(R.string.privacy_policy),
            icon = { Icon(Icons.Outlined.Policy, contentDescription = null) },
        )
        MpServiceNavButton(
            text = stringResource(R.string.terms_of_use),
            icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
        )
        MpServiceNavButton(
            text = stringResource(R.string.acknowledgements),
            icon = { Icon(Icons.Outlined.VolunteerActivism, contentDescription = null) },
        )
        MpVersionRow()
        HorizontalDivider()

        MpSignOutButton()
    }
}

// before: features/settings/SettingsView.kt#Preview
@Preview(name = "SettingsView", showBackground = true, widthDp = 411)
@Composable
private fun Material_SettingsView() {
    Theme {
        Surface {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = { MpBackButton() },
                    windowInsets = NoInsets,
                )
                MpSettingsContent()
            }
        }
    }
}

// before: features/settings/SettingsView.kt#Preview (ServiceNavButton has no standalone @Preview)
@Preview(name = "ServiceNavButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_ServiceNavButton() {
    Theme {
        Surface {
            Column {
                MpServiceNavButton(
                    text = stringResource(R.string.notifications_title),
                    icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                )
                MpServiceNavButton(stringResource(R.string.feed), { MpLogo(R.drawable.sparcs_logo) })
            }
        }
    }
}

// before: features/settings/SettingsView.kt#Preview (SignOutButton has no standalone @Preview)
@Preview(name = "SignOutButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_SignOutButton() {
    Theme { Surface { MpSignOutButton() } }
}

// before: features/settings/SettingsView.kt#Preview (ThemeSwitcherButton has no standalone @Preview)
@Preview(name = "ThemeSwitcherButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_ThemeSwitcherButton() {
    Theme { Surface { MpThemeSwitcherButton(darkMode = false) } }
}

// before: features/settings/SettingsView.kt#Preview (AppSettings has no standalone @Preview)
@Preview(name = "AppSettings", showBackground = true, widthDp = 411)
@Composable
private fun Material_AppSettings() {
    Theme { Surface { MpAppSettings() } }
}

// before: features/settings/SettingsView.kt#Preview (FeedbackButton has no standalone @Preview)
@Preview(name = "FeedbackButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedbackButton() {
    Theme { Surface { MpFeedbackButton() } }
}

// before: features/settings/SettingsView.kt#Preview (SendCrashReportsButton has no standalone @Preview)
@Preview(name = "SendCrashReportsButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_SendCrashReportsButton() {
    Theme {
        Surface {
            var checked by remember { mutableStateOf(false) }
            MpSendCrashReportsButton(checked) { checked = it }
        }
    }
}

// before: features/settings/SettingsView.kt#Preview (VersionRow has no standalone @Preview)
@Preview(name = "VersionRow", showBackground = true, widthDp = 411)
@Composable
private fun Material_VersionRow() {
    Theme { Surface { MpVersionRow() } }
}

// endregion

// region RowElementView + Taxi settings

@Composable
private fun MpRowElementView(title: String, content: String) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Text(content) },
    )
}

// before: features/settings/components/RowElementView.kt#Preview
@Preview(name = "RowElementView", showBackground = true, widthDp = 411)
@Composable
private fun Material_RowElementView() {
    Theme { Surface { MpRowElementView(title = "Title", content = "Content") } }
}

@Composable
private fun MpTaxiSettingsTopBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.taxi_settings)) },
        navigationIcon = { MpBackButton() },
        actions = {
            TextButton(onClick = {}, enabled = false) { Text(stringResource(R.string.done)) }
        },
        windowInsets = NoInsets,
    )
}

@Composable
private fun MpTaxiSettingsLoaded() {
    // Same data as TaxiSettingsView.kt#PreviewTaxiSettingsLoaded.
    val viewModel = remember {
        MockTaxiSettingsViewModel(TaxiSettingsViewModel.ViewState.Loaded).apply {
            user = TaxiUser.mock()
            bankName = user?.account?.split(" ")?.firstOrNull()
            bankNumber = user?.account?.split(" ")?.getOrNull(1) ?: ""
        }
    }
    var nickname by remember { mutableStateOf(viewModel.nickname ?: "") }
    var bankName by remember { mutableStateOf(viewModel.bankName) }
    var bankNumber by remember { mutableStateOf(viewModel.bankNumber) }
    var phoneNumber by remember { mutableStateOf(viewModel.phoneNumber) }
    var showBadge by remember { mutableStateOf(viewModel.showBadge) }
    var residence by remember { mutableStateOf(viewModel.residence) }
    val hasNumberRegistered = viewModel.user?.phoneNumber?.isEmpty() == false

    Column(Modifier.fillMaxWidth()) {
        MpSectionHeader(stringResource(R.string.profile))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(R.string.nickname)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            MpReadOnlyDropdownField(
                label = stringResource(R.string.bank_name),
                value = bankName ?: "",
                placeholder = stringResource(R.string.select_bank),
                options = Constants.taxiBankNameList,
                onSelect = { bankName = it },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bankNumber,
                onValueChange = { input -> bankNumber = input.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.enter_bank_number)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input -> phoneNumber = input.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.enter_phone_number)) },
                enabled = !hasNumberRegistered,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (hasNumberRegistered) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.show_badge)) },
                supportingContent = { Text(stringResource(R.string.members_with_this_badge)) },
                trailingContent = { Switch(checked = showBadge, onCheckedChange = null) },
                modifier = Modifier.toggleable(
                    value = showBadge,
                    role = Role.Switch,
                    onValueChange = { showBadge = it },
                ),
            )
        }
        OutlinedTextField(
            value = residence,
            onValueChange = { residence = it },
            label = { Text(stringResource(R.string.enter_residence)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        MpSectionHeader(stringResource(R.string.services))
        ListItem(
            headlineContent = { Text(stringResource(R.string.report_details)) },
            leadingContent = { Icon(Icons.Outlined.SmsFailed, contentDescription = null) },
            modifier = Modifier.clickable {},
        )
    }
}

// before: features/settings/taxi/TaxiSettingsView.kt#PreviewTaxiSettingsLoaded
@Preview(name = "TaxiSettingsView", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiSettingsView() {
    Theme {
        Surface {
            Column {
                MpTaxiSettingsTopBar()
                MpTaxiSettingsLoaded()
            }
        }
    }
}

// before: features/settings/taxi/TaxiSettingsView.kt#PreviewTaxiSettingsLoading
@Preview(name = "TaxiSettingsView", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiSettingsView_Loading() {
    Theme {
        Surface {
            Column {
                MpTaxiSettingsTopBar()
                MpRowElementView(stringResource(R.string.nickname), stringResource(R.string.unknown))
                MpRowElementView(stringResource(R.string.bank_name), stringResource(R.string.unknown))
                MpRowElementView(stringResource(R.string.phone_number), stringResource(R.string.unknown))
            }
        }
    }
}

// endregion

// region Search

// before: shared/views/contentViews/SearchBar.kt#Preview
@Preview(name = "SearchCustomBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_SearchCustomBar() {
    Theme {
        Surface {
            var query by remember { mutableStateOf("sasd") }
            MpSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun MpCourseFilterChips(
    filter: CourseFilterState,
    showReset: Boolean,
    showPeriod: Boolean,
    showTime: Boolean,
    isTimeSelected: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showReset) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.filter_reset)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }

        CourseFilterCategory.entries
            .filter { showPeriod || it != CourseFilterCategory.Period }
            .forEach { category ->
                val selectedOptionIds = filter.selectedOptionIds(category)
                val isSelected = selectedOptionIds.isNotEmpty()
                val options = CourseFilterProvider.getOptions(category)
                val selectedLabels = if (category == CourseFilterCategory.Department) {
                    filter.departments.map { it.code }
                } else {
                    selectedOptionIds.map { id -> options.find { it.id == id }?.label ?: id }
                }
                val baseLabel = stringResource(category.labelResId)
                val extraCount = selectedLabels.size - 2
                val othersText = stringResource(R.string.and_n_others, extraCount.coerceAtLeast(0))
                val labelText = when {
                    !isSelected -> baseLabel
                    extraCount > 0 -> "$baseLabel: ${selectedLabels.take(2).joinToString(", ")} $othersText"
                    else -> "$baseLabel: ${selectedLabels.joinToString(", ")}"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {},
                    label = { Text(labelText) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else null,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                )
            }

        if (showTime) {
            FilterChip(
                selected = isTimeSelected,
                onClick = {},
                label = { Text(stringResource(R.string.lecture_time_filter)) },
                leadingIcon = if (isTimeSelected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else null,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}

// before: features/lectureSearch/components/LectureSearchField.kt#SearchFieldPreview
@Preview(name = "LectureSearchField", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureSearchField() {
    Theme {
        Surface {
            var query by remember { mutableStateOf("") }
            val filter = CourseFilterState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MpSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = stringResource(R.string.search_by_course),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                MpCourseFilterChips(
                    filter = filter,
                    showReset = !filter.isEmpty(),
                    showPeriod = false,
                    showTime = true,
                    isTimeSelected = false,
                )
            }
        }
    }
}

// before: features/search/components/CourseFilterRow.kt#Preview
@Preview(name = "CourseFilterRow", showBackground = true, widthDp = 411)
@Composable
private fun Material_CourseFilterRow() {
    Theme {
        Surface {
            val filter = CourseFilterState(
                classifications = listOf("MR", "BR", "BE"),
                departments = listOf(DepartmentOption(9945, "School of Computing", "CS")),
                levels = emptyList(),
                period = "0",
            )
            MpCourseFilterChips(
                filter = filter,
                showReset = !filter.isEmpty(),
                showPeriod = true,
                showTime = false,
            )
        }
    }
}

// before: features/lectureSearch/components/LectureSearchChrome.kt#NavigationPreview
@Preview(name = "LectureSearchNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureSearchNavigationBar() {
    Theme {
        Surface {
            TopAppBar(
                title = { Text("Timetable", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { MpBackButton() },
                windowInsets = NoInsets,
            )
        }
    }
}

// endregion

// region Comment / chat input

@Composable
private fun MpCommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showAvatar: Boolean,
    isAnonymous: Boolean? = null,
    onAnonymousChange: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = if (showAvatar) {
                { Icon(Icons.Default.AccountCircle, contentDescription = null) }
            } else null,
            trailingIcon = {
                IconButton(onClick = {}, enabled = value.isNotEmpty()) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                    )
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            maxLines = 6,
        )
        if (isAnonymous != null) {
            FilterChip(
                selected = isAnonymous,
                onClick = { onAnonymousChange(!isAnonymous) },
                label = { Text(stringResource(R.string.anonymous)) },
                leadingIcon = if (isAnonymous) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else null,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// before: shared/views/contentViews/CommentInputBar.kt (no @Preview; as used by features/post/PostView.kt PostInputBar)
@Preview(name = "CommentInputBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_CommentInputBar() {
    Theme {
        Surface {
            var text by remember { mutableStateOf("") }
            MpCommentInputBar(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(R.string.write_a_comment),
                showAvatar = true,
            )
        }
    }
}

// before: shared/views/contentViews/CommentInputBar.kt (no @Preview; as used by features/feedPost/FeedPostView.kt with the anonymous toggle)
@Preview(name = "CommentInputBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_CommentInputBar_Feed() {
    Theme {
        Surface {
            var text by remember { mutableStateOf("Great post!") }
            var isAnonymous by remember { mutableStateOf(true) }
            MpCommentInputBar(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(R.string.write_a_comment),
                showAvatar = false,
                isAnonymous = isAnonymous,
                onAnonymousChange = { isAnonymous = it },
            )
        }
    }
}

// before: features/taxiChat/components/TaxiChatInputBar.kt#Preview
@Preview(name = "TaxiChatInputBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiChatInputBar() {
    Theme {
        Surface {
            var text by remember { mutableStateOf("text") }
            var expanded by remember { mutableStateOf(false) }
            val taxiUser = TaxiUser.mock()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Add, contentDescription = "More")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.photo_library)) },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.request_settlement)) },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.send_payment)) },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Outlined.Payment, contentDescription = null) },
                        )
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            stringResource(R.string.chat_as_user, taxiUser.nickname),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    maxLines = 6,
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = {}, enabled = text.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send))
                }
            }
        }
    }
}

// endregion

// region Taxi room creation

@Composable
private fun MpTaxiCapacityPicker(capacity: Int, onCapacityChange: (Int) -> Unit) {
    val options = listOf(2, 3, 4)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.capacity), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, count ->
                SegmentedButton(
                    selected = count == capacity,
                    onClick = { onCapacityChange(count) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    label = { Text(pluralStringResource(R.plurals.people_count, count, count)) },
                )
            }
        }
    }
}

// before: features/taxiRoomCreation/components/TaxiCapacityPicker.kt#Preview
@Preview(name = "TaxiCapacityPicker", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiCapacityPicker() {
    Theme {
        Surface {
            var capacity by remember { mutableStateOf(4) }
            MpTaxiCapacityPicker(capacity) { capacity = it }
        }
    }
}

@Composable
private fun MpTaxiCarrierToggle(hasCarrier: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.carrier_status_title)) },
        supportingContent = {
            Text(
                if (hasCarrier) stringResource(R.string.carrier_status_yes)
                else stringResource(R.string.carrier_status_no)
            )
        },
        leadingContent = { Icon(Icons.Outlined.Luggage, contentDescription = null) },
        trailingContent = { Switch(checked = hasCarrier, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = hasCarrier,
            role = Role.Switch,
            onValueChange = onToggle,
        ),
    )
}

// before: features/taxiRoomCreation/components/TaxiCarrierToggleButton.kt#PreviewWithCarrier
@Preview(name = "TaxiCarrierToggleButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiCarrierToggleButton() {
    Theme {
        Surface {
            var hasCarrier by remember { mutableStateOf(true) }
            MpTaxiCarrierToggle(hasCarrier) { hasCarrier = it }
        }
    }
}

// before: features/taxiRoomCreation/components/TaxiCarrierToggleButton.kt#PreviewWithoutCarrier
@Preview(name = "TaxiCarrierToggleButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiCarrierToggleButton_Without() {
    Theme {
        Surface {
            var hasCarrier by remember { mutableStateOf(false) }
            MpTaxiCarrierToggle(hasCarrier) { hasCarrier = it }
        }
    }
}

private enum class MpDeparturePanel { None, Date, Time }

@Composable
private fun MpReadOnlyField(
    label: String,
    value: String,
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        label = { Text(label) },
        trailingIcon = { Icon(icon, contentDescription = null) },
        colors = if (active) {
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            )
        } else OutlinedTextFieldDefaults.colors(),
        modifier = modifier,
    )
}

@Composable
private fun MpTaxiDepartureTimePicker(panel: MpDeparturePanel) {
    // Same data as TaxiDepartureTimePicker.kt#Preview: now + 1 day.
    val departure = remember { LocalDateTime.now().plusDays(1) }
    val today = remember { LocalDate.now() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.departure_time), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MpReadOnlyField(
                label = stringResource(R.string.date),
                value = departure.format(DateTimeFormatter.ofPattern("MM/dd")),
                icon = Icons.Outlined.Event,
                active = panel == MpDeparturePanel.Date,
                modifier = Modifier.weight(1f),
            )
            MpReadOnlyField(
                label = "Time",
                value = departure.format(DateTimeFormatter.ofPattern("HH:mm")),
                icon = Icons.Outlined.Schedule,
                active = panel == MpDeparturePanel.Time,
                modifier = Modifier.weight(1f),
            )
        }
        when (panel) {
            MpDeparturePanel.None -> Unit
            MpDeparturePanel.Date -> {
                val selectableDates = remember(today) {
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                            return !day.isBefore(today) && !day.isAfter(today.plusDays(13))
                        }
                    }
                }
                val state = rememberDatePickerState(
                    initialSelectedDateMillis = departure.toLocalDate()
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                    selectableDates = selectableDates,
                )
                DatePicker(state = state, showModeToggle = false)
            }

            MpDeparturePanel.Time -> {
                val state = rememberTimePickerState(
                    initialHour = departure.hour,
                    initialMinute = departure.minute,
                    is24Hour = true,
                )
                Spacer(Modifier.height(16.dp))
                TimeInput(state = state, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

// before: features/taxiRoomCreation/components/TaxiDepartureTimePicker.kt#Preview
@Preview(name = "TaxiDepartureTimePicker", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiDepartureTimePicker() {
    Theme { Surface { MpTaxiDepartureTimePicker(MpDeparturePanel.None) } }
}

// before: features/taxiRoomCreation/components/TaxiDepartureTimePicker.kt#Preview (expanded date state, CustomDatePicker)
@Preview(name = "TaxiDepartureTimePicker", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiDepartureTimePicker_Date() {
    Theme { Surface { MpTaxiDepartureTimePicker(MpDeparturePanel.Date) } }
}

// before: features/taxiRoomCreation/components/TaxiDepartureTimePicker.kt#Preview (expanded time state, CircularTimePicker)
@Preview(name = "TaxiDepartureTimePicker", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiDepartureTimePicker_Time() {
    Theme { Surface { MpTaxiDepartureTimePicker(MpDeparturePanel.Time) } }
}

@Composable
private fun MpTaxiDestinationPicker(
    source: TaxiLocation?,
    destination: TaxiLocation?,
    locations: List<TaxiLocation>,
    favoriteRoutes: List<TaxiFavoriteRoute>,
) {
    var from by remember { mutableStateOf(source) }
    var to by remember { mutableStateOf(destination) }
    val titles = locations.map { it.title.localized() }
    val canAddFavorite = from != null && to != null && from?.id != to?.id &&
            favoriteRoutes.none { it.from.id == from?.id && it.to.id == to?.id }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MpReadOnlyDropdownField(
                    label = stringResource(R.string.meeting_point),
                    value = from?.title?.localized() ?: "",
                    options = titles,
                    onSelect = { title -> from = locations.firstOrNull { it.title.localized() == title } },
                    modifier = Modifier.fillMaxWidth(),
                )
                MpReadOnlyDropdownField(
                    label = stringResource(R.string.where_to),
                    value = to?.title?.localized() ?: "",
                    options = titles,
                    onSelect = { title -> to = locations.firstOrNull { it.title.localized() == title } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IconButton(onClick = {
                val previousFrom = from
                from = to
                to = previousFrom
            }) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap")
            }
        }

        MpSectionHeader(stringResource(R.string.favorite_routes))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (favoriteRoutes.isEmpty()) {
                Text(
                    stringResource(R.string.no_favorite_routes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            favoriteRoutes.forEach { favorite ->
                AssistChip(
                    onClick = {
                        from = favorite.from
                        to = favorite.to
                    },
                    label = { Text("${favorite.from.title.localized()} → ${favorite.to.title.localized()}") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
            }
            if (canAddFavorite) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.add_to_favorites)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// before: features/taxiRoomCreation/components/TaxiDestinationPicker.kt#Preview
@Preview(name = "TaxiDestinationPicker", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiDestinationPicker() {
    Theme {
        Surface {
            MpTaxiDestinationPicker(
                source = TaxiLocation.mock(),
                destination = TaxiLocation.mock(),
                locations = TaxiLocation.mockList(),
                favoriteRoutes = listOf(
                    TaxiFavoriteRoute(
                        id = "1",
                        from = TaxiLocation.mock(),
                        to = TaxiLocation.mockList()[1],
                    )
                ),
            )
        }
    }
}

// endregion

// region Taxi list week selector

// before: features/taxiList/components/WeekDaySelector.kt#WeekDaySelectorPreview
@Preview(name = "WeekDaySelector", showBackground = true, widthDp = 411)
@Composable
private fun Material_WeekDaySelector() {
    Theme {
        Surface {
            // Same data as TaxiListViewModel.week: today + the next 6 days; selectedDate = null ("All").
            val today = remember { LocalDate.now() }
            val week = remember(today) { (0L until 7L).map { today.plusDays(it) } }
            var selected by remember { mutableStateOf<LocalDate?>(null) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selected == null,
                    onClick = { selected = null },
                    label = { Text(stringResource(R.string.all)) },
                    leadingIcon = if (selected == null) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else null,
                )
                week.forEach { day ->
                    val isSelected = day == selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { selected = day },
                        label = { Text("${day.dayOfWeek.toShortString().uppercase()} ${day.dayOfMonth}") },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        } else null,
                    )
                }
            }
        }
    }
}

// endregion

// region Timetable selector

// before: features/timetable/components/CompactTimetableSelector.kt#Preview
@Preview(name = "CompactTimetableSelector", showBackground = true, widthDp = 411)
@Composable
private fun Material_CompactTimetableSelector() {
    Theme {
        Surface {
            // Same data as PreviewTimetableViewModel: Semester.mockList()[9], selected table id 1.
            val semesters = remember { Semester.otlMockList() }
            val tables = remember { TimetableSummary.otlMockList() }
            var selectedSemester by remember { mutableStateOf(semesters[9]) }
            val tableName = tables.firstOrNull { it.id == 1 }?.title ?: stringResource(R.string.my_table)
            var semesterExpanded by remember { mutableStateOf(false) }
            var tableExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { semesterExpanded = true },
                        label = { Text(selectedSemester.description) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        },
                    )
                    DropdownMenu(expanded = semesterExpanded, onDismissRequest = { semesterExpanded = false }) {
                        semesters.forEach { semester ->
                            DropdownMenuItem(
                                text = { Text(semester.description) },
                                onClick = {
                                    selectedSemester = semester
                                    semesterExpanded = false
                                },
                            )
                        }
                    }
                }
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { tableExpanded = true },
                        label = { Text(tableName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        },
                    )
                    DropdownMenu(expanded = tableExpanded, onDismissRequest = { tableExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.my_table)) },
                            onClick = { tableExpanded = false },
                        )
                        tables.forEach { table ->
                            DropdownMenuItem(
                                text = { Text(table.title.ifEmpty { stringResource(R.string.untitled) }) },
                                onClick = { tableExpanded = false },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.timetable_add)) },
                            onClick = { tableExpanded = false },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }
        }
    }
}

// before: features/timetable/components/CompactTimetableSelector.kt#Preview (dropdown-field variant)
@Preview(name = "CompactTimetableSelector", showBackground = true, widthDp = 411)
@Composable
private fun Material_CompactTimetableSelector_Dropdown() {
    Theme {
        Surface {
            val semesters = remember { Semester.otlMockList() }
            val tables = remember { TimetableSummary.otlMockList() }
            val semesterLabels = semesters.map { it.description }
            var semesterText by remember { mutableStateOf<String?>(null) }
            var tableName by remember { mutableStateOf(tables.first { it.id == 1 }.title) }
            val myTable = stringResource(R.string.my_table)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MpReadOnlyDropdownField(
                    label = stringResource(R.string.semester),
                    value = semesterText ?: semesterLabels[9],
                    options = semesterLabels,
                    onSelect = { semesterText = it },
                    modifier = Modifier.weight(1f),
                )
                MpReadOnlyDropdownField(
                    label = stringResource(R.string.timetable),
                    value = tableName,
                    options = listOf(myTable) + tables.map { it.title },
                    onSelect = { tableName = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// endregion

// region Course filter sheets

@Composable
private fun MpCategoryFilterContent(
    category: CourseFilterCategory,
    initialFilters: CourseFilterState,
    options: List<CourseFilterOption>,
) {
    var filters by remember { mutableStateOf(initialFilters) }
    val selectedIds = filters.selectedOptionIds(category)
    val selectedCount = selectedIds.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val title = stringResource(category.labelResId)
            val subtitle = if (selectedCount > 0) " " + stringResource(R.string.selected_count, selectedCount) else ""
            Text(title + subtitle, style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = { filters = filters.withSelection(category, emptyList()) },
                enabled = selectedCount > 0,
            ) {
                Text(stringResource(R.string.reset))
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val isSelected = option.id in selectedIds
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val next = when {
                            category == CourseFilterCategory.Period -> if (isSelected) emptyList() else listOf(option.id)
                            isSelected -> selectedIds - option.id
                            else -> selectedIds + option.id
                        }
                        filters = filters.withSelection(category, next)
                    },
                    label = { Text(option.label) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else null,
                )
            }
        }
    }
}

// before: shared/views/contentViews/CategoryFilterContent.kt#Preview
@Preview(name = "CategoryFilterContent", showBackground = true, widthDp = 411)
@Composable
private fun Material_CategoryFilterContent() {
    Theme {
        Surface {
            MpCategoryFilterContent(
                category = CourseFilterCategory.Classification,
                initialFilters = CourseFilterState(classifications = listOf("MR", "ME")),
                options = listOf(
                    CourseFilterOption("BR", "BR"),
                    CourseFilterOption("MR", "MR"),
                    CourseFilterOption("ME", "ME"),
                    CourseFilterOption("HSE", "HSE"),
                ),
            )
        }
    }
}

@Composable
private fun MpDepartmentSelectionSections(
    departments: List<DepartmentOption>,
    interestedDepartmentIDs: Set<Int>,
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val keyword = query.trim()
    val groups: List<Pair<Int?, List<DepartmentOption>>> = remember(departments, interestedDepartmentIDs, keyword) {
        if (keyword.isNotEmpty()) {
            val (exact, others) = departments
                .filter { it.name.contains(keyword, true) || it.code.contains(keyword, true) }
                .partition { it.code.equals(keyword, true) }
            listOf(null to exact + others)
        } else {
            val (interested, others) = departments.partition { it.id in interestedDepartmentIDs }
            if (interested.isEmpty()) listOf(null to others)
            else listOf(R.string.interested_departments to interested, R.string.other_departments to others)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        DockedSearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(R.string.department_search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                )
            },
            expanded = false,
            onExpandedChange = {},
            modifier = Modifier.fillMaxWidth(),
        ) {}
        groups.forEach { (titleRes, options) ->
            if (options.isNotEmpty()) {
                if (titleRes != null) {
                    MpSectionHeader(stringResource(titleRes))
                }
                options.forEach { department ->
                    val selected = department.id in selectedDepartmentIDs
                    ListItem(
                        headlineContent = { Text(department.name) },
                        supportingContent = { Text(department.code) },
                        trailingContent = { Checkbox(checked = selected, onCheckedChange = null) },
                        modifier = Modifier.toggleable(
                            value = selected,
                            role = Role.Checkbox,
                            onValueChange = {
                                onSelectionChange(
                                    if (selected) selectedDepartmentIDs - department.id
                                    else selectedDepartmentIDs + department.id
                                )
                            },
                        ),
                    )
                }
            }
        }
    }
}

// before: shared/views/contentViews/DepartmentPicker.kt#DepartmentPickerContentPreview
@Preview(name = "DepartmentPickerContent", showBackground = true, widthDp = 411)
@Composable
private fun Material_DepartmentPickerContent() {
    Theme {
        Surface {
            var selected by remember { mutableStateOf(setOf(1)) }
            Column(Modifier.padding(16.dp)) {
                TextButton(onClick = { selected = emptySet() }, enabled = selected.isNotEmpty()) {
                    Text(stringResource(R.string.reset))
                }
                MpDepartmentSelectionSections(
                    departments = listOf(
                        DepartmentOption(1, "Computer Science", "CS"),
                        DepartmentOption(2, "Mathematics", "MAS"),
                    ),
                    interestedDepartmentIDs = setOf(2),
                    selectedDepartmentIDs = selected,
                    onSelectionChange = { selected = it },
                )
            }
        }
    }
}

// before: shared/views/contentViews/DepartmentPicker.kt#DepartmentSelectionSectionsPreview
@Preview(name = "DepartmentSelectionSections", showBackground = true, widthDp = 411)
@Composable
private fun Material_DepartmentSelectionSections() {
    Theme {
        Surface {
            var selected by remember { mutableStateOf(setOf(1)) }
            MpDepartmentSelectionSections(
                departments = listOf(
                    DepartmentOption(1, "Computer Science", "CS"),
                    DepartmentOption(2, "Mathematics", "MAS"),
                ),
                interestedDepartmentIDs = setOf(1),
                selectedDepartmentIDs = selected,
                onSelectionChange = { selected = it },
            )
        }
    }
}

// endregion
