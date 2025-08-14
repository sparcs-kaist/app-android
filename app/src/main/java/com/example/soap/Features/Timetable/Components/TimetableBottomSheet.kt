package com.example.soap.Features.Timetable.Components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.grayF8
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun TimetableBottomSheet() {
    val configuration = LocalConfiguration.current
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var searchCourse by remember { mutableStateOf("") }

    val anchors = listOf(
        (screenHeight) * 0.9f,
        (screenHeight) * 0.5f,
        (screenHeight) * 0.1f
    )

    val offsetY = remember { mutableStateOf(anchors[0]) }
    val scope = rememberCoroutineScope()

    Box{

        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .fillMaxWidth()
                .height((screenHeight - offsetY.value).dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        offsetY.value =
                            (offsetY.value + delta).coerceIn(anchors.first(), anchors.last())
                    },
                    onDragStopped = { velocity ->
                        val nearest = anchors.minByOrNull { abs(it - offsetY.value) }!!
                        scope.launch {
                            animate(
                                initialValue = offsetY.value,
                                targetValue = nearest,
                                animationSpec = tween(250)
                            ) { value, _ -> offsetY.value = value }
                        }
                    }
                )
                .padding(16.dp)
        ) {
            Column {
                Box(Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)){
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(40.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.darkGray, RoundedCornerShape(2.dp))
                    )
                }

                SearchCourses(value = searchCourse, onValueChange = { searchCourse = it })

                Spacer(Modifier.padding(4.dp))

                //화면
                //학년/학과/구분+버튼
                //강의 내용

            }
        }
    }
}


@Composable
private fun SearchCourses(
    value: String,
    onValueChange: (String)-> Unit
){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.grayF8),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = "Send Button",
                tint = MaterialTheme.colorScheme.grayBB
            )

            Spacer(Modifier.padding(4.dp))

            Box(
                Modifier
                    .weight(1f)
                    .padding(4.dp),
                ) {

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_by_course),
                                    color = MaterialTheme.colorScheme.grayBB,
                                    style = MaterialTheme.typography.bodyLarge
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
        Column(Modifier.fillMaxSize()) { TimetableBottomSheet() }
    }
}