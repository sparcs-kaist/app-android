package com.example.soap.Features.PostCompose.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.PostList.PostListViewModel
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors


@Composable
fun FlairSelector(
    postListViewModel: PostListViewModel,
){
    var selectedFlair by remember { mutableStateOf("No flair") }
    var expanded by remember { mutableStateOf(false) }

    var previousFlair by remember { mutableStateOf("No flair") }

    if (previousFlair != selectedFlair) {
        previousFlair = selectedFlair
    }

    Card(
        colors = CardDefaults.cardColors(MaterialTheme.soapColors.grayf8),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable { expanded = !expanded }
        ) {

            AnimatedAlphabetText(
                from = getLocalizedFlair(previousFlair),
                to = getLocalizedFlair(selectedFlair)
            )

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.baseline_arrow_drop_down),
                contentDescription = "Change Flair",
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
            modifier = Modifier.background(MaterialTheme.soapColors.background)
        ){
            Box(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable {
                        selectedFlair = "No flair"
                        expanded = false
                    }
            ) {
                Text(
                    text = getLocalizedFlair("No flair"),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.soapColors.grayBB
                )

            }

            HorizontalDivider(thickness = 1.dp)

            postListViewModel.flairList.forEachIndexed{ index, flair ->

                Box(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectedFlair = flair
                            expanded = false
                        }
                ) {
                    Text(
                        text = getLocalizedFlair(flair),
                        style = MaterialTheme.typography.titleSmall
                    )

                }
                if (index != postListViewModel.flairList.lastIndex) {
                    HorizontalDivider(thickness = 1.dp)
                }
            }

        }
    }
}

@Composable
fun getLocalizedFlair(flair: String): String {
    val flairMap = mapOf(
        "No flair" to R.string.no_flair,
        "All" to R.string.all,
        "SPPANGS" to R.string.sppangs,
        "Meal" to R.string.meal,
        "Money" to R.string.money,
        "Gaming" to R.string.gaming,
        "Dating" to R.string.dating,
        "Lost & Found" to R.string.lost_and_found
    )

    val resId = flairMap[flair] ?: return flair
    return stringResource(id = resId)
}


@Composable
fun AnimatedAlphabetText(from: String, to: String) {
    val maxLength = maxOf(from.length, to.length)

    Row {
        for (i in 0 until maxLength) {
            val toChar = to.getOrNull(i) ?: ' '
            val color = if (to == getLocalizedFlair("No flair")) MaterialTheme.soapColors.grayBB else MaterialTheme.soapColors.onSurface

            AnimatedContent(
                targetState = toChar,
                transitionSpec = {
                    val slideIn = slideInVertically { it }
                    val fadeIn = fadeIn(initialAlpha = 0.3f)

                    val slideOut = slideOutVertically { -it }
                    val fadeOut = fadeOut()

                    (slideIn + fadeIn).togetherWith((slideOut + fadeOut))
                },
                label = "charTransition"
            ) { char ->
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}




@Composable
@Preview
private fun Preview(){
    SoapTheme{ FlairSelector(viewModel()) }
}