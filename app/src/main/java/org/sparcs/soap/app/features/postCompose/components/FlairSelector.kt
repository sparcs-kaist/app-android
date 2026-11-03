package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemShapes
import androidx.compose.material3.SelectableDropdownMenuItem
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
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
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

        ExpressiveMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val topics = viewModel.board.topics
            val count = topics.size + 1
            MenuGroup(index = 0, count = 1, maxHeight = 320.dp) {
                TopicMenuItem(stringResource(R.string.no_topic), selectedTopic == null, MenuDefaults.itemShape(0, count)) {
                    viewModel.selectedTopic = null
                    expanded = false
                }
                topics.forEachIndexed { index, topic ->
                    TopicMenuItem(topic.name.localized(), selectedTopic == topic, MenuDefaults.itemShape(index + 1, count)) {
                        viewModel.selectedTopic = topic
                        expanded = false
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicMenuItem(name: String, selected: Boolean, shapes: MenuItemShapes, onClick: () -> Unit) {
    SelectableDropdownMenuItem(
        selected = selected,
        text = { Text(name) },
        onClick = onClick,
        shapes = shapes,
        selectedLeadingIcon = {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
        },
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { TopicSelector(MockPostComposeViewModel()) }
}
