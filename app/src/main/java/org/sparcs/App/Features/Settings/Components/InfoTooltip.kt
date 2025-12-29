package org.sparcs.App.Features.Settings.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoTooltip(
    tooltipText: String,
    icon: Painter? = null,
    imageVector: ImageVector? = null,
    tint: Color = Color.Unspecified,
    iconSize: Dp = 20.dp
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            RichTooltip {
                Text(text = tooltipText)
            }
        },
        state = tooltipState
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    scope.launch { tooltipState.show() }
                }
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = tint
                )
            } else if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = tint
                )
            }
        }
    }
}