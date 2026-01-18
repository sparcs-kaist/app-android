package org.sparcs.App.Features.Post.Components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.darkGray
import org.sparcs.R
import org.sparcs.soap.Features.Post.PostCommentActionsMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostNavigationBar(
    boardGroup: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onReport: (AraContentReportType) -> Unit,
    onTranslate: () -> Unit,
    isMine: Boolean?
) {
    TopAppBar(
        navigationIcon = {
            TextButton(
                onClick = { onClick() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_ios),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                    Text(
                        text = boardGroup,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.darkGray
                    )
                }
            }
        },
        title = {},

        actions = {
            PostCommentActionsMenu(
                enumClass = AraContentReportType::class,
                isMine = isMine,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate,
                isComment = false,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.padding(4.dp))
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

}

@Composable
@Preview
private fun Preview(){
    Theme{ PostNavigationBar("Board", {}, {}, {}, {}, false) }
}