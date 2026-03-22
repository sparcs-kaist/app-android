package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatReadReceipt
import org.sparcs.soap.App.Features.TaxiChat.Components.MetadataVisibility
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import org.sparcs.soap.App.Features.TaxiChat.Components.SenderInfo
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun MessageView(
    chat: TaxiChat,
    kind: TaxiChat.ChatType,
    sender: SenderInfo,
    position: ChatBubblePosition,
    readCount: Int,
    metadata: MetadataVisibility,
    hasBadge: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showPopover by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (sender.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (sender.isMine) {
            ChatReadReceipt(
                readCount = readCount,
                showTime = metadata.showTime,
                time = chat.time,
                alignment = Alignment.End
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            ChatAvatarImage(
                sender = sender,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .alpha(if (metadata.showAvatar) 1f else 0f)
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 350.dp),
            horizontalAlignment = if (sender.isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (metadata.showName) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AuthorNamePlace(sender)

                    if (hasBadge) {
                        Box {
                            Icon(
                                painter = painterResource(R.drawable.phone_circle_fill),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showPopover = true }
                            )

                            if (showPopover) {
                                Popup(
                                    alignment = Alignment.TopCenter,
                                    onDismissRequest = { showPopover = false }
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.width(250.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.members_with_this_badge),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (sender.isMine) Arrangement.End else Arrangement.Start
            ) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    content()
                }

                if (!sender.isMine) {
                    ChatReadReceipt(
                        readCount = readCount,
                        showTime = metadata.showTime,
                        time = chat.time,
                        alignment = Alignment.Start
                    )
                    Spacer(modifier = Modifier.widthIn(min = 40.dp))
                }
            }
        }
    }
}

@Composable
private fun AuthorNamePlace(sender: SenderInfo) {
    val name = when {
        sender.isWithdrew -> stringResource(R.string.unknown)
        sender.name != null -> sender.name
        sender.id == null -> stringResource(R.string.taxi_bot)
        else -> stringResource(R.string.unknown)
    }

    Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, name = "MessageView Comparison")
@Composable
private fun PreviewMessageView() {
    Theme {
        MessageView(
            chat = TaxiChat.mock(),
            kind = TaxiChat.ChatType.TEXT,
            sender = SenderInfo(
                id = "other",
                name = "택시 동료",
                avatarURL = null,
                isMine = true,
                isWithdrew = false
            ),
            position = ChatBubblePosition.MIDDLE,
            readCount = 2,
            metadata = MetadataVisibility(showAvatar = false, showName = false, showTime = true),
            hasBadge = true
        ) {
            ChatBubble(TaxiChat.mockList()[1], ChatBubblePosition.MIDDLE, false)
        }
    }
}