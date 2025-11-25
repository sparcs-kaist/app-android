package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import java.net.URL
import java.util.Date

@Composable
fun TaxiChatUserWrapper(
    authorID: String?,
    authorName: String?,
    authorProfileImageURL: URL?,
    date: Date?,
    isMe: Boolean,
    isGeneral: Boolean,
    isWithdrawn: Boolean,
    content: @Composable () -> Unit
) {
    if (isGeneral) {
        content()
    } else {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // profile picture
            if (!isMe) {
                when {
                    isWithdrawn -> UnknownProfileImage()
                    authorID == null -> BotProfileImage()
                    else -> UserProfileImage(authorProfileImageURL)
                }
            } else {
                // spacer for me
                Spacer(modifier = Modifier.width(60.dp))
            }

            Column(
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                // nickname label
                if (!isMe) {
                    Text(
                        text = when {
                            isWithdrawn -> stringResource(R.string.unknown)
                            authorName != null -> authorName
                            authorID == null -> stringResource(R.string.taxi_bot)
                            else -> stringResource(R.string.unknown)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                // chat bubbles
                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    content()
                }
            }

            // spacer for other users
            if (!isMe) {
                Spacer(modifier = Modifier.width(60.dp))
            }
        }
    }
}

@Composable
fun UserProfileImage(url: URL?) {
    if (url != null) {
        val painter = rememberAsyncImagePainter(url.toString())
        Image(
            painter = painter,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )
    }
}

@Composable
fun BotProfileImage() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("🤖", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun UnknownProfileImage() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("👻", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
        ) {
            TaxiChatUserWrapper(
                authorID = "",
                authorName = "alex",
                authorProfileImageURL = null,
                date = Date(),
                isMe = false,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "hey", showTip = true, isMe = false)
            }

            TaxiChatUserWrapper(
                authorID = "",
                authorName = "jordan",
                authorProfileImageURL = null,
                date = Date(),
                isMe = true,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "hey alex!", showTip = true, isMe = true)
            }

            TaxiChatUserWrapper(
                authorID = "",
                authorName = "sam",
                authorProfileImageURL = null,
                date = Date(),
                isMe = false,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "yo everyone", showTip = false, isMe = false)
                TaxiChatBubble(content = "what's up", showTip = true, isMe = false)
            }

            TaxiChatUserWrapper(
                authorID = "",
                authorName = "alex",
                authorProfileImageURL = null,
                date = Date(),
                isMe = false,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "how's your day going?", showTip = true, isMe = false)
            }

            TaxiChatUserWrapper(
                authorID = "",
                authorName = "sam",
                authorProfileImageURL = null,
                date = Date(),
                isMe = false,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "pretty chill", showTip = false, isMe = false)
                TaxiChatBubble(content = "so far", showTip = false, isMe = false)
                TaxiChatBubble(content = "might hit the gym later.", showTip = true, isMe = false)
            }

            TaxiChatUserWrapper(
                authorID = "",
                authorName = "jordan",
                authorProfileImageURL = null,
                date = Date(),
                isMe = true,
                isGeneral = false,
                isWithdrawn = false
            ) {
                TaxiChatBubble(content = "same here", showTip = false, isMe = true)
                TaxiChatBubble(content = "just working through emails", showTip = true, isMe = true)
                TaxiChatBubble(content = "just working emails", showTip = true, isMe = true)
            }
        }
    }
}
