package com.sparcs.soap.Features.NavigationBar.Animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.delay


fun trendingEnterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        slideInHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            initialOffsetX = { it }
        )
    }

fun trendingExitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        slideOutHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            targetOffsetX = { it }
        )
    }

fun trendingPopEnterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        slideInHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            initialOffsetX = { it }
        )
    }

fun trendingPopExitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        slideOutHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            targetOffsetX = { it }
        )
    }

@Composable
fun MoveToLeftFadeOut(
    show: Boolean,
    content: @Composable () -> Unit
){
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(
            initialOffsetX = { -40 },
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
            targetOffsetX = { -40 },
            animationSpec = tween(200)
        )
    ){
        content()
    }
}

@Composable
fun MoveToLeftFadeIn(
    show: Boolean,
    content: @Composable () -> Unit
){
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(
            initialOffsetX = { +40 },
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
            targetOffsetX = { +40 },
            animationSpec = tween(200)
        )
    ){
        content()
    }
}

@Composable
fun AnimatedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    perCharDelay: Long = 30L
) {
    var previous by remember { mutableStateOf(text) }
    var displayed by remember { mutableStateOf(text) }

    LaunchedEffect(text) {
        val maxLength = maxOf(previous.length, text.length)
        val fromChars = previous.padEnd(maxLength)
        val toChars = text.padEnd(maxLength)
        var temp = fromChars.toCharArray()

        for (i in 0 until maxLength) {
            temp[i] = toChars[i]
            displayed = temp.concatToString()
            delay(perCharDelay)
        }

        previous = text
    }

    Row {
        for (char in displayed) {
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn())
                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "charTransition"
            ) { c ->
                Text(
                    text = c.toString(),
                    style = style,
                    fontWeight = fontWeight,
                    color = color
                )
            }
        }
    }
}
