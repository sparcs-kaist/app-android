package org.sparcs.Features.PostCompose.Components
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.sparcs.Domain.Helpers.LocalizedString
import org.sparcs.Features.PostCompose.PostComposeViewModelProtocol
import org.sparcs.R
import org.sparcs.Shared.ViewModelMocks.Ara.MockPostComposeViewModel
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.grayBB

@Composable
fun TopicSelector(viewModel: PostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }
    var previousTopic by remember { mutableStateOf<LocalizedString?>(null) }
    val selectedTopic = viewModel.selectedTopic

    if (previousTopic != selectedTopic?.name) {
        previousTopic = selectedTopic?.name
    }

    val displayText = selectedTopic?.name?.localized() ?: stringResource(R.string.no_topic)
    val previousText = previousTopic?.localized() ?: stringResource(R.string.no_topic)

    Box {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .clickable { expanded = !expanded }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                AnimatedAlphabetText(
                    from = previousText,
                    to = displayText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_drop_down),
                    contentDescription = "Change Topic"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text =  stringResource(R.string.no_topic),
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
fun AnimatedAlphabetText(from: String, to: String) {
    val maxLength = maxOf(from.length, to.length)
    var displayed by remember { mutableStateOf(from.padEnd(maxLength)) }
    var previous by remember { mutableStateOf(from.padEnd(maxLength)) }

    LaunchedEffect(to) {
        val toChars = to.padEnd(maxLength)
        val temp = previous.padEnd(maxLength).toCharArray()

        for (i in 0 until maxLength) {
            temp[i] = toChars[i]
            displayed = temp.concatToString()
            delay(30L)
        }

        previous = toChars
    }

    Row {
        val noTopicString = stringResource(R.string.no_topic)

        for (i in 0 until maxLength) {
            val toChar = displayed.getOrNull(i) ?: ' '
            val color =
                if (displayed ==  noTopicString) MaterialTheme.colorScheme.grayBB
                else MaterialTheme.colorScheme.onSurface

            AnimatedContent(
                targetState = toChar,
                transitionSpec = {
                    val slideIn = slideInVertically { it }
                    val fadeIn = fadeIn(initialAlpha = 0.3f)
                    val slideOut = slideOutVertically { -it }
                    val fadeOut = fadeOut()
                    (slideIn + fadeIn).togetherWith(slideOut + fadeOut)
                },
                label = "charTransition"
            ) { char ->
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}


@Composable
@Preview
private fun Preview(){
    Theme{ TopicSelector(MockPostComposeViewModel()) }
}
