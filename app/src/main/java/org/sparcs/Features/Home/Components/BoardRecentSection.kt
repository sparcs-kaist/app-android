package org.sparcs.Features.Home.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.R
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.darkGray
import org.sparcs.ui.theme.grayBB


@Composable
fun BoardRecentSection(
    title : String,
    clickAction : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(4.dp)
                .clickable { clickAction() }
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = "Go to $title Board",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.darkGray
            )
        }
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Column {
                        Text(
                            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Lorem ipsum\t13 min ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.grayBB
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        BoardRecentSection("title", clickAction = {})
    }
}