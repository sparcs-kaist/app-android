package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.sparcs.soap.App.Domain.Models.Taxi.SenderInfo
import org.sparcs.soap.App.theme.ui.Theme

@Composable
fun ChatAvatarImage(
    sender: SenderInfo,
    modifier: Modifier = Modifier
) {
    when {
        sender.isWithdrew -> {
            UnknownAvatarImage(modifier)
        }
        sender.id == null -> {
            BotAvatarImage(modifier)
        }
        else -> {
            UserAvatarImage(sender.avatarURL, modifier)
        }
    }
}

@Composable
private fun UserAvatarImage(
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun BotAvatarImage(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
    ) {
        Text(text = "🤖", fontSize = 20.sp)
    }
}

@Composable
private fun UnknownAvatarImage(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
    ) {
        Text(text = "👻", fontSize = 20.sp)
    }
}

@Preview(showBackground = true, name = "Normal User Avatar")
@Composable
private fun NormalUserAvatarPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatAvatarImage(
                sender = SenderInfo(
                    id = "user_id",
                    name = "Gemini",
                    avatarURL = "https://example.com/image.jpg",
                    isMine = false,
                    isWithdrew = false
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Withdrew User Avatar")
@Composable
private fun WithdrewUserAvatarPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatAvatarImage(
                sender = SenderInfo(
                    id = "withdrew_id",
                    name = "Unknown",
                    avatarURL = null,
                    isMine = false,
                    isWithdrew = true
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Bot Avatar")
@Composable
private fun BotAvatarPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatAvatarImage(
                sender = SenderInfo(
                    id = null,
                    name = "System Bot",
                    avatarURL = null,
                    isMine = false,
                    isWithdrew = false
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty URL User Avatar")
@Composable
private fun EmptyUrlUserAvatarPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatAvatarImage(
                sender = SenderInfo(
                    id = "no_image_id",
                    name = "No Image",
                    avatarURL = null,
                    isMine = false,
                    isWithdrew = false
                )
            )
        }
    }
}