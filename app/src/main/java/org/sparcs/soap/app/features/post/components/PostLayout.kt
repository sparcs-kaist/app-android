package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun usesPostPanes(): Boolean = LocalConfiguration.current.screenWidthDp.dp >= POST_MIN_WIDTH + COMMENTS_MIN_WIDTH

@Composable
fun PostColumn(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            content = content,
        )
    }
}

@Composable
fun PostPanes(
    post: @Composable () -> Unit,
    comments: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).widthIn(min = POST_MIN_WIDTH).fillMaxHeight()) { post() }
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Column(
            Modifier
                .widthIn(min = COMMENTS_MIN_WIDTH, max = COMMENTS_MAX_WIDTH)
                .fillMaxHeight()
        ) {
            comments()
        }
    }
}

private val POST_MIN_WIDTH = 380.dp
private val COMMENTS_MIN_WIDTH = 320.dp
private val COMMENTS_MAX_WIDTH = 420.dp

@Preview(showBackground = true, widthDp = 900, heightDp = 480)
@Composable
private fun PostPanesPreview() {
    Theme {
        PostPanes(
            post = { PostColumn(rememberLazyListState()) { item { Text("Post") } } },
            comments = { PostColumn(rememberLazyListState()) { item { Text("Comments") } } },
        )
    }
}
