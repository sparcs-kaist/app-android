package com.example.soap.Shared.Views.ContentViews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray

@Composable
fun SearchView(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
        navController: NavController
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.darkGray,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                InputBar(
                    text = query,
                    onTextChange = onQueryChange,
                    onSearch = onSearch
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}
@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
                //Text input
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (text.isNotBlank()) onSearch(text)
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (text.isEmpty()) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.padding(2.dp))
                                Text(
                                    text = "Search",
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
    }



@Composable
@Preview
private fun Preview(){
    Theme {
        SearchView(
            query = "",
            onQueryChange = {},
            onSearch = {},
            content = {
                Text(text = "Content")
            },
            navController = rememberNavController()
        )
    }
}