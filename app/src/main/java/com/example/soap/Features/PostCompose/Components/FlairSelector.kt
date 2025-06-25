package com.example.soap.Features.PostCompose.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.PostList.PostListViewModel
import com.example.soap.R


@Composable
fun FlairSelector(
    postListViewModel: PostListViewModel,
){
    var selectedFlair by remember { mutableStateOf("No flair") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(Color(0xFFF2F2F6)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable { expanded = !expanded }
        ) {
            AnimatedContent(
                targetState = selectedFlair,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut())
                    } using SizeTransform(clip = false)
                },
                label ="selectedFlairTextAnimation"
            ) { targetFlair ->
                Text(
                    text = targetFlair,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.baseline_arrow_drop_down),
                contentDescription = "Change Flair",
            )
        }

    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {expanded = false}
    ){
        postListViewModel.flairList.forEach{ flair ->
            Box(
                Modifier
                    .padding(8.dp)
                    .clickable {
                        selectedFlair = flair
                        expanded = false
                    }
            ) {
                Text(
                    text = flair,
                    style = MaterialTheme.typography.titleSmall
                )

            }
            HorizontalDivider(thickness = 1.dp)

        }

    }
}

@Composable
@Preview
private fun Preview(){
    FlairSelector(viewModel())
}