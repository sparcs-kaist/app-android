package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB

@Composable
fun TopicSelector(viewModel: PostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTopic = viewModel.selectedTopic

    Box {
        ElevatedCard(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .clickable { expanded = !expanded }
                .glassBorder(shape = RoundedCornerShape(20.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = selectedTopic?.name?.localized() ?: stringResource(R.string.no_topic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTopic == null) MaterialTheme.colorScheme.grayBB
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Change Topic"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(16.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.no_topic),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                onClick = {
                    viewModel.selectedTopic = null
                    expanded = false
                },
            )

            viewModel.board.topics.forEach { topic ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = topic.name.localized(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    onClick = {
                        viewModel.selectedTopic = topic
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { TopicSelector(MockPostComposeViewModel()) }
}
