package org.sparcs.soap.app.features.taxiChatList.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatListViewNavigationBar(
    onDismiss: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        navigationIcon = { BackButton(onClick = { onDismiss() }) },
        title = { Text(text = stringResource(R.string.chats)) },
        scrollBehavior = scrollBehavior
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview(){
    Theme {
        TaxiChatListViewNavigationBar(onDismiss = {})
    }
}

