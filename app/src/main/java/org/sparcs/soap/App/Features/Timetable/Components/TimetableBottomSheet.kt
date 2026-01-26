package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Shared.Extensions.noRippleClickable
import org.sparcs.soap.App.theme.ui.darkGray
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun TimetableBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable (onFold: () -> Unit) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    val anchors = listOf(
        (screenHeight) * 0.9f, //접히는 기준
        (screenHeight) * 0.8f, //가장 많이 접힌 상태
        (screenHeight) * 0.5f, //중간
        (screenHeight) * 0.1f //화면 가득
    )

    val imeInsets = WindowInsets.ime
    val imeBottom = imeInsets.getBottom(LocalDensity.current)

    val offsetY = remember { Animatable(anchors[2]) }
    val scope = rememberCoroutineScope()

    val onFold: () -> Unit = {
        scope.launch {
            offsetY.animateTo(
                targetValue = anchors[1],
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .noRippleClickable { onDismiss() }
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(0, (offsetY.value - imeBottom).roundToInt()) }
                .fillMaxWidth()
                .height(((screenHeight - offsetY.value + imeBottom) / LocalDensity.current.density).dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val minOffset = anchors.minOrNull() ?: 0f
                        val maxOffset = anchors.maxOrNull() ?: screenHeight
                        scope.launch {
                            offsetY.snapTo((offsetY.value + delta).coerceIn(minOffset, maxOffset))
                        }
                    },
                    onDragStopped = {
                        val nearest = anchors.minByOrNull { abs(it - offsetY.value) }!!
                        scope.launch {
                            offsetY.animateTo(
                                targetValue = nearest,
                                animationSpec = tween(250)
                            )
                        }
                        if (nearest == anchors[0]) {
                            onDismiss()
                        }
                    }
                )
                .padding(top = 16.dp)
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.darkGray,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                content(onFold)
            }
        }
    }
}