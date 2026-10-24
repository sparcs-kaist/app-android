package org.sparcs.soap.app.features.navigationBar.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import kotlinx.coroutines.delay


// Motion for NavHost destinations. Pushing slides the new screen in over the old one, which
// shifts slightly the other way (parallax); popping is the exact reverse, so during a predictive
// back gesture the top screen slides away with the finger. Switching between the main tabs uses
// Material fade through. All specs are tweens so the navigation library can seek them while the
// user drags the gesture.

private const val MOTION_DURATION = 300
private const val FADE_OUT_DURATION = 90
private val emphasized = FastOutSlowInEasing

/** How far the screen underneath shifts, as a fraction of the container width. */
private fun parallax(fullWidth: Int) = fullWidth / 4

private fun slideIn(forward: Boolean): EnterTransition =
    slideInHorizontally(tween(MOTION_DURATION, easing = emphasized)) {
        if (forward) it else -parallax(it)
    }

private fun slideOut(forward: Boolean): ExitTransition =
    slideOutHorizontally(tween(MOTION_DURATION, easing = emphasized)) {
        if (forward) -parallax(it) else it
    }

private val fadeThroughIn: EnterTransition =
    fadeIn(tween(MOTION_DURATION - FADE_OUT_DURATION, FADE_OUT_DURATION, LinearOutSlowInEasing)) +
        scaleIn(tween(MOTION_DURATION - FADE_OUT_DURATION, FADE_OUT_DURATION, emphasized), initialScale = 0.92f)

private val fadeThroughOut: ExitTransition =
    fadeOut(tween(FADE_OUT_DURATION, easing = FastOutLinearInEasing))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(isTabRoot: (NavDestination) -> Boolean) =
    isTabRoot(initialState.destination) && isTabRoot(targetState.destination)

fun navEnterTransition(isTabRoot: (NavDestination) -> Boolean):
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (isTabSwitch(isTabRoot)) fadeThroughIn else slideIn(forward = true)
}

fun navExitTransition(isTabRoot: (NavDestination) -> Boolean):
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isTabSwitch(isTabRoot)) fadeThroughOut else slideOut(forward = true)
}

fun navPopEnterTransition(isTabRoot: (NavDestination) -> Boolean):
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (isTabSwitch(isTabRoot)) fadeThroughIn else slideIn(forward = false)
}

fun navPopExitTransition(isTabRoot: (NavDestination) -> Boolean):
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isTabSwitch(isTabRoot)) fadeThroughOut else slideOut(forward = false)
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
        val temp = fromChars.toCharArray()

        for (i in 0 until maxLength) {
            temp[i] = toChars[i]
            displayed = temp.concatToString()
            delay(perCharDelay)
        }

        previous = text
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
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
