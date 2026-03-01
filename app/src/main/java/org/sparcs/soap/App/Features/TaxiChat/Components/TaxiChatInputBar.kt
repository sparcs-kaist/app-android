package org.sparcs.soap.App.Features.TaxiChat.Components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.R


@Composable
fun TaxiChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendText: (String) -> Unit,
    onSendImage: (Bitmap) -> Unit,
    isUploading: Boolean,
    isCommitPaymentAvailable: Boolean,
    isCommitSettlementAvailable: Boolean,
    onCommitPayment: () -> Unit,
    onCommitSettlement: () -> Unit,
    taxiUser: TaxiUser?,
    modifier: Modifier = Modifier,
) {
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val bitmap = context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            selectedImage = bitmap
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.Bottom
    ) {
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Add, contentDescription = "More")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        launcher.launch("image/*")
                        expanded = false
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.photo_library),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenuItem(
                    onClick = {
                        onCommitSettlement()
                        expanded = false
                    },
                    enabled = isCommitSettlementAvailable,
                    text = {
                        Text(
                            text = stringResource(R.string.request_settlement),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenuItem(
                    onClick = {
                        onCommitPayment()
                        expanded = false
                    },
                    enabled = isCommitPaymentAvailable,
                    text = {
                        Text(
                            text = stringResource(R.string.send_payment),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Payment,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (selectedImage != null) {
                Box {
                    Image(
                        bitmap = selectedImage!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    IconButton(
                        onClick = { selectedImage = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White
                        )
                    }
                }
            } else {
                //Text input
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                val nickname =
                                    taxiUser?.nickname ?: stringResource(R.string.unknown)
                                Text(
                                    text = stringResource(R.string.chat_as_user, nickname),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send button
        Button(
            onClick = {
                if (selectedImage != null) {
                    onSendImage(selectedImage!!)
                    selectedImage = null
                } else if (text.isNotBlank()) {
                    onSendText(text)
                    onTextChange("")
                }
            },
            enabled = text.isNotBlank() || selectedImage != null
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.outline_send),
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp),
                    tint = if (text.isNotBlank() || selectedImage != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}