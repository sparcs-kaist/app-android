package org.sparcs.soap.app.features.navigationBar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** Up navigation for pushed screens. */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

/** Dismisses a full-screen dialog such as a compose or creation screen. */
@Composable
fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.close)
        )
    }
}

@Composable
fun AddButton(
    contentDescription: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    IconButton(onClick = onClick, enabled = isEnabled) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = contentDescription)
    }
}

@Composable
fun ChatButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = stringResource(R.string.chats)
        )
    }
}

@Composable
fun SearchButton(
    onClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    IconToggleButton(checked = isSelected, onCheckedChange = { onClick() }) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.search)
        )
    }
}

/** Confirming text action for a full-screen dialog's top app bar, e.g. "Post" or "Save". */
@Composable
fun TopBarTextAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (isBusy) {
        Box(modifier = modifier.padding(horizontal = 16.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.semantics { contentDescription = text }
        ) {
            Text(text)
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        SearchButton({}, false)
    }
}
