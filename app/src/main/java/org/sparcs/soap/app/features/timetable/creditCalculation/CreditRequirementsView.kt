package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.theme.ui.Theme

private data class RequirementRow(
    val key: String,
    val title: String,
    val taken: Int,
    val minimum: Int?,
    val update: (CreditRequirements, Int) -> CreditRequirements = { requirements, _ -> requirements },
)

private data class RequirementGroup(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val rows: List<RequirementRow>,
)

@Composable
internal fun CreditRequirementsView(
    state: CreditCalculationViewState,
    onBack: () -> Unit,
    onSave: (CreditRequirements) -> Unit,
) {
    val rows = requirementRows(state)
    val groups = buildList {
        add(
            RequirementGroup(
                "graduation",
                stringResource(R.string.credit_graduation),
                Icons.Outlined.School,
                rows.filter { it.key == "graduation" })
        )
        add(
            RequirementGroup(
                "basic", stringResource(R.string.credit_basic),
                Icons.AutoMirrored.Outlined.MenuBook, rows.filter { it.key in listOf("br", "be") })
        )
        state.creditBreakdown.majors.forEach { group ->
            val id = group.department.id
            add(
                RequirementGroup(
                    "major.$id", group.department.name, Icons.Outlined.Apartment,
                    rows.filter { it.key == "mr.$id" || it.key == "me.$id" })
            )
        }
        add(
            RequirementGroup(
                "hse",
                stringResource(R.string.credit_humanities),
                Icons.Outlined.People,
                rows.filter { it.key.startsWith("hse") })
        )
        add(
            RequirementGroup(
                "au", "AU",
                Icons.AutoMirrored.Outlined.DirectionsRun, rows.filter { it.key == "au" })
        )
        rows.filter { it.key == "etc" }.takeIf { it.isNotEmpty() }?.let {
            add(
                RequirementGroup(
                    "etc",
                    stringResource(R.string.lecture_type_etc_full),
                    Icons.Outlined.MoreHoriz,
                    it
                )
            )
        }
    }
    var editingKey by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = rows.find { it.key == editingKey }
    CreditScreen(stringResource(R.string.credit_requirements), onBack) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(
                    stringResource(R.string.credit_requirements_notice),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            items(groups, key = { it.key }) { group ->
                CreditCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            group.icon,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            group.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    group.rows.forEach { row -> RequirementProgress(row) { editingKey = row.key } }
                }
            }
            item {
                TextButton(onClick = { onSave(CreditRequirements()) }) { Text(stringResource(R.string.credit_reset)) }
            }
        }
    }
    if (editing != null) {
        CreditRequirementEditor(
            editing.title,
            editing.minimum ?: 0,
            { editingKey = null }) { minimum ->
            onSave(editing.update(state.requirements, minimum))
            editingKey = null
        }
    }
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
                requirements.graduation
            ) { r, n -> r.copy(graduation = n) })
        add(
            RequirementRow(
                "br",
                stringResource(R.string.lecture_type_br_full),
                breakdown.basicRequired,
                requirements.basicRequired
            ) { r, n -> r.copy(basicRequired = n) })
        add(
            RequirementRow(
                "be",
                stringResource(R.string.lecture_type_be_full),
                breakdown.basicElective,
                requirements.basicElective
            ) { r, n -> r.copy(basicElective = n) })
        breakdown.majors.forEach { group ->
            val department = group.department
            add(
                RequirementRow(
                    "mr.${department.id}",
                    "${department.name} · ${stringResource(R.string.mr)}",
                    group.required,
                    requirements.majorRequired(department)
                ) { r, n ->
                    r.copy(majorRequired = r.majorRequired + (department.id to n))
                })
            add(
                RequirementRow(
                    "me.${department.id}",
                    "${department.name} · ${stringResource(R.string.me)}",
                    group.elective,
                    requirements.majorElective(department)
                ) { r, n ->
                    r.copy(majorElective = r.majorElective + (department.id to n))
                })
        }
        add(
            RequirementRow(
                "hseCore",
                stringResource(R.string.lecture_type_hse_core_full),
                breakdown.hseCore,
                requirements.hseCore
            ) { r, n -> r.copy(hseCore = n) })
        add(
            RequirementRow(
                "hseGeneral",
                stringResource(R.string.lecture_type_hse_general_full),
                breakdown.hseGeneral,
                requirements.hseGeneral
            ) { r, n -> r.copy(hseGeneral = n) })
        if (breakdown.hse > 0) add(
            RequirementRow(
                "hse",
                stringResource(R.string.lecture_type_hse_full),
                breakdown.hse,
                null
            )
        )
        add(RequirementRow("au", "AU", breakdown.au, requirements.au) { r, n -> r.copy(au = n) })
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

@Composable
private fun RequirementProgress(row: RequirementRow, onEdit: () -> Unit) {
    val isMet = row.minimum?.let { row.taken >= it } == true
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                row.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (isMet) Icon(
                Icons.Rounded.CheckCircle, stringResource(R.string.credit_requirement_met),
                tint = CreditCompleteColor, modifier = Modifier.size(18.dp)
            )
            Text(
                "${row.taken}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            row.minimum?.let {
                Text(
                    "/$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        row.minimum?.let { minimum ->
            CreditProgressBar(row.taken, minimum)
            TextButton(onClick = onEdit, modifier = Modifier.align(Alignment.End)) {
                Text(
                    stringResource(R.string.credit_edit)
                )
            }
        }
    }
}

@Composable
private fun CreditRequirementEditor(
    title: String,
    minimum: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var value by rememberSaveable(title) { mutableStateOf(minimum.toString()) }
    val number = value.toIntOrNull()?.takeIf { it >= 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.credit_requirements)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = number == null
                )
                if (number == null) Text(
                    stringResource(R.string.credit_invalid_minimum),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { number?.let(onSave) },
                enabled = number != null
            ) { Text(stringResource(R.string.credit_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.credit_cancel)) } }
    )
}

@Preview
@Composable
private fun CreditRequirementsPreview() {
    Theme { CreditRequirementsView(creditPreviewState(), {}, {}) }
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
            ), {})
    }
}

@Preview
@Composable
private fun CreditRequirementEditorPreview() {
    Theme { CreditRequirementEditor(stringResource(R.string.credit_total), 138, {}, {}) }
}
