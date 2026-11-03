package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TopicSelector(viewModel: PostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTopic = viewModel.selectedTopic

    Box {
        ElevatedFilterChip(
            selected = false,
            onClick = { expanded = !expanded },
            label = {
                Text(
                    text = selectedTopic?.name?.localized() ?: stringResource(R.string.no_topic),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TopicMenuItem(stringResource(R.string.no_topic), selectedTopic == null) {
                viewModel.selectedTopic = null
                expanded = false
            }
            viewModel.board.topics.forEach { topic ->
                TopicMenuItem(topic.name.localized(), selectedTopic == topic) {
                    viewModel.selectedTopic = topic
                    expanded = false
                }
            }
        }
    }
}

@Composable
private fun TopicMenuItem(name: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(name) },
        trailingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null,
        onClick = onClick
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { TopicSelector(MockPostComposeViewModel()) }
}
