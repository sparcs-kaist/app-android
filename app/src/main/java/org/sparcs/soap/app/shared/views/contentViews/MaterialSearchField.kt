package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Material docked search bar used as a plain search field: it filters content in place and never
 * expands into a results view. [autoFocus] opens the keyboard when the field first appears.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onQueryChange(it.replace("\n", "")) },
                onSearch = { keyboard?.hide() },
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear_search_text)
                            )
                        }
                    }
                } else null,
                // The field caps itself at 720dp; fill the bar so the clear button sits at its end
                // on wide landscape screens.
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChange(it.isFocused) }
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier.fillMaxWidth()
    ) {}
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme { MaterialSearchField("query", {}, "Search") }
}
