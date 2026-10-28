package org.sparcs.soap.app.features.timetable.creditCalculation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditRequirementsNavigationBar(
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.credit_requirements))
        },
        navigationIcon = {
            BackButton(onClick = onBack)
        },
        actions = {
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.credit_edit))
                }
            }
        }
    )
}

@Preview
@Composable
private fun CreditRequirementsNavigationBarPreview() {
    Theme {
        CreditRequirementsNavigationBar(onBack = {})
    }
}
