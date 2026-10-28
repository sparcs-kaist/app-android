package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.features.navigationBar.components.CloseButton
import org.sparcs.soap.app.features.navigationBar.components.TopBarTextAction
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.timetable.creditCalculation.components.CreditRequirementsNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.creditCompleteColor
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewCreditCalculationViewModel

private data class RequirementRow(
    val key: String,
    val title: String,
    val taken: Int,
    val minimum: Int?,
    val update: (CreditRequirements, Int) -> CreditRequirements = { requirements, _ -> requirements },
    /** Reads this row's minimum from a set of requirements; null for rows without one. */
    val read: ((CreditRequirements) -> Int)? = null,
)

private data class RequirementGroup(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val rows: List<RequirementRow>,
)

@Composable
internal fun CreditRequirementsView(
    viewModel: CreditCalculationViewModelProtocol,
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    Box(Modifier.analyticsScreen("CreditRequirements")) {
        CreditRequirementsContent(state, { navController.popBackStack() }, viewModel::updateRequirements)
        GlobalAlertDialog(
            isPresented = viewModel.isAlertPresented,
            state = viewModel.alertState,
            onDismiss = { viewModel.isAlertPresented = false },
        )
    }
}

@Composable
internal fun CreditRequirementsContent(
    state: CreditCalculationViewState,
    onBack: () -> Unit,
    onSave: (CreditRequirements) -> Unit,
) {
    val rows = requirementRows(state)
    val groups = requirementGroups(state, rows)
    var isEditing by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CreditRequirementsNavigationBar(onBack = onBack, onEdit = { isEditing = true })
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val landscape = isLandscape()
            val columns = if (landscape) 2 else 1
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = if (landscape) 1080.dp else 720.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection) + 16.dp,
                    end = padding.calculateEndPadding(layoutDirection) + 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.credit_requirements_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                items(groups.chunked(columns), key = { it.first().key }) { row ->
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { group ->
                            RequirementGroupCard(
                                group,
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
    if (isEditing) {
        CreditRequirementsEditor(
            groups = groups,
            requirements = state.requirements,
            onDismiss = { isEditing = false },
            onSave = {
                onSave(it)
                isEditing = false
            }
        )
    }
}

@Composable
private fun RequirementGroupCard(
    group: RequirementGroup,
    modifier: Modifier = Modifier,
) {
    CreditCard(modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                group.icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                group.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
        }
        group.rows.forEach { row -> RequirementProgress(row) }
    }
}

@Composable
private fun requirementGroups(
    state: CreditCalculationViewState,
    rows: List<RequirementRow>,
): List<RequirementGroup> {
    val rowsByKey = rows.associateBy { it.key }
    fun group(key: String, title: String, icon: ImageVector, vararg rowKeys: String) =
        RequirementGroup(key, title, icon, rowKeys.mapNotNull(rowsByKey::get))

    val general = listOf(
        group(
            "graduation",
            stringResource(R.string.credit_graduation),
            Icons.Outlined.School,
            "graduation"
        ),
        group(
            "basic",
            stringResource(R.string.credit_basic),
            Icons.AutoMirrored.Outlined.MenuBook,
            "br",
            "be"
        ),
    )
    val majors = state.creditBreakdown.majors.map { major ->
        val department = major.department
        group(
            "major.${department.id}", department.name, Icons.Outlined.Apartment,
            "mr.${department.id}", "me.${department.id}"
        )
    }
    val remaining = listOf(
        group(
            "hse", stringResource(R.string.credit_humanities), Icons.Outlined.People,
            "hseCore", "hseGeneral", "hse"
        ),
        group("au", "AU", Icons.AutoMirrored.Outlined.DirectionsRun, "au"),
        group(
            "etc",
            stringResource(R.string.lecture_type_etc_full),
            Icons.Outlined.MoreHoriz,
            "etc"
        ),
    )
    return (general + majors + remaining).filter { it.rows.isNotEmpty() }
}

@Composable
private fun requirementRows(state: CreditCalculationViewState): List<RequirementRow> {
    val breakdown = state.creditBreakdown
    val requirements = state.requirements
    return buildList {
        add(
            RequirementRow(
                "graduation",
                stringResource(R.string.credit_total),
                state.overallSummary.earnedCredits,
                requirements.graduation,
                update = { r, n -> r.copy(graduation = n) },
                read = { it.graduation }
            ))
        add(
            RequirementRow(
                "br",
                stringResource(R.string.lecture_type_br_full),
                breakdown.basicRequired,
                requirements.basicRequired,
                update = { r, n -> r.copy(basicRequired = n) },
                read = { it.basicRequired }
            ))
        add(
            RequirementRow(
                "be",
                stringResource(R.string.lecture_type_be_full),
                breakdown.basicElective,
                requirements.basicElective,
                update = { r, n -> r.copy(basicElective = n) },
                read = { it.basicElective }
            ))
        breakdown.majors.forEach { group ->
            val department = group.department
            add(
                RequirementRow(
                    "mr.${department.id}",
                    "${department.name} · ${stringResource(R.string.mr)}",
                    group.required,
                    requirements.majorRequired(department),
                    update = { r, n -> r.copy(majorRequired = r.majorRequired + (department.id to n)) },
                    read = { it.majorRequired(department) }
                ))
            add(
                RequirementRow(
                    "me.${department.id}",
                    "${department.name} · ${stringResource(R.string.me)}",
                    group.elective,
                    requirements.majorElective(department),
                    update = { r, n -> r.copy(majorElective = r.majorElective + (department.id to n)) },
                    read = { it.majorElective(department) }
                ))
        }
        add(
            RequirementRow(
                "hseCore",
                stringResource(R.string.lecture_type_hse_core_full),
                breakdown.hseCore,
                requirements.hseCore,
                update = { r, n -> r.copy(hseCore = n) },
                read = { it.hseCore }
            ))
        add(
            RequirementRow(
                "hseGeneral",
                stringResource(R.string.lecture_type_hse_general_full),
                breakdown.hseGeneral,
                requirements.hseGeneral,
                update = { r, n -> r.copy(hseGeneral = n) },
                read = { it.hseGeneral }
            ))
        if (breakdown.hse > 0) add(
            RequirementRow(
                "hse",
                stringResource(R.string.lecture_type_hse_full),
                breakdown.hse,
                null
            )
        )
        add(
            RequirementRow(
                "au", "AU", breakdown.au, requirements.au,
                update = { r, n -> r.copy(au = n) },
                read = { it.au }
            )
        )
        if (breakdown.etc > 0) add(
            RequirementRow(
                "etc",
                stringResource(R.string.lecture_type_etc_full),
                breakdown.etc,
                null
            )
        )
    }
}

/** One requirement: title, credits taken over the minimum, and a progress bar. */
@Composable
private fun RequirementProgress(row: RequirementRow) {
    val isMet = row.minimum?.let { row.taken >= it } == true
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                row.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (isMet) Icon(
                Icons.Filled.CheckCircle, stringResource(R.string.credit_requirement_met),
                tint = creditCompleteColor, modifier = Modifier.size(18.dp)
            )
            Text("${row.taken}", style = MaterialTheme.typography.titleMedium)
            row.minimum?.let {
                Text(
                    "/$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        row.minimum?.let { minimum -> CreditProgressBar(row.taken, minimum) }
    }
}

/**
 * Full-screen editor for every minimum at once, grouped like the cards. Changes apply on Save;
 * Reset to defaults fills in the standard minimums without saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditRequirementsEditor(
    groups: List<RequirementGroup>,
    requirements: CreditRequirements,
    onDismiss: () -> Unit,
    onSave: (CreditRequirements) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CreditRequirementsEditorContent(groups, requirements, onDismiss, onSave)
    }
}

/** Saves the editor's drafts as a flat key, value, key, value… list. */
private val DraftsSaver = listSaver<SnapshotStateMap<String, String>, String>(
    save = { drafts -> drafts.flatMap { (key, value) -> listOf(key, value) } },
    restore = { flat ->
        mutableStateMapOf<String, String>().apply {
            flat.chunked(2).forEach { (key, value) -> put(key, value) }
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditRequirementsEditorContent(
    groups: List<RequirementGroup>,
    requirements: CreditRequirements,
    onDismiss: () -> Unit,
    onSave: (CreditRequirements) -> Unit,
) {
    val editable = groups
        .map { group -> group to group.rows.filter { it.read != null } }
        .filter { (_, rows) -> rows.isNotEmpty() }
    val rows = editable.flatMap { it.second }
    // Saveable so typed values survive rotation.
    val drafts = rememberSaveable(saver = DraftsSaver) {
        mutableStateMapOf<String, String>().apply {
            rows.forEach { put(it.key, it.read!!(requirements).toString()) }
        }
    }
    fun parsed(key: String) = drafts[key]?.toIntOrNull()?.takeIf { it >= 0 }
    val isValid = rows.all { parsed(it.key) != null }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { CloseButton(onDismiss) },
                title = { Text(stringResource(R.string.credit_minimums_title)) },
                actions = {
                    TopBarTextAction(
                        text = stringResource(R.string.credit_save),
                        enabled = isValid,
                        onClick = {
                            onSave(rows.fold(requirements) { acc, row -> row.update(acc, parsed(row.key)!!) })
                        }
                    )
                }
            )
        },
        modifier = Modifier.imePadding()
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            editable.forEach { (group, groupRows) ->
                item(key = "header-${group.key}") {
                    SettingsSectionHeader(group.title, icon = group.icon)
                }
                items(groupRows, key = { it.key }) { row ->
                    val value = drafts[row.key].orEmpty()
                    val isError = parsed(row.key) == null
                    OutlinedTextField(
                        value = value,
                        onValueChange = { drafts[row.key] = it.filter(Char::isDigit) },
                        label = { Text(row.title) },
                        singleLine = true,
                        isError = isError,
                        supportingText = if (isError) {
                            { Text(stringResource(R.string.credit_invalid_minimum)) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            item(key = "reset") {
                TextButton(
                    onClick = {
                        val defaults = CreditRequirements()
                        rows.forEach { drafts[it.key] = it.read!!(defaults).toString() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                ) { Text(stringResource(R.string.credit_reset)) }
            }
        }
    }
}

@Preview
@Preview(device = "spec:width=891dp,height=411dp,orientation=landscape")
@Composable
private fun CreditRequirementsPreview() {
    val viewModel = remember { PreviewCreditCalculationViewModel() }
    Theme { CreditRequirementsView(viewModel, rememberNavController()) }
}

@Preview
@Composable
private fun RequirementGroupCardPreview() {
    Theme {
        RequirementGroupCard(
            RequirementGroup(
                "basic",
                stringResource(R.string.credit_basic),
                Icons.AutoMirrored.Outlined.MenuBook,
                listOf(
                    RequirementRow("br", stringResource(R.string.lecture_type_br_full), 25, 23),
                    RequirementRow("be", stringResource(R.string.lecture_type_be_full), 6, 9)
                )
            )
        )
    }
}

@Preview
@Composable
private fun RequirementProgressPreview() {
    Theme {
        RequirementProgress(
            RequirementRow(
                "br",
                stringResource(R.string.lecture_type_br_full),
                25,
                23
            )
        )
    }
}

@Preview
@Composable
private fun CreditRequirementsEditorPreview() {
    val state = creditPreviewState()
    val groups = requirementGroups(state, requirementRows(state))
    Theme { CreditRequirementsEditorContent(groups, state.requirements, {}, {}) }
}
